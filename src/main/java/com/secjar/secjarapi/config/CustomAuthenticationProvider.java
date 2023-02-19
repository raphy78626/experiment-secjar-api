package com.secjar.secjarapi.config;

import com.secjar.secjarapi.exceptions.EmailNotVerifiedException;
import com.secjar.secjarapi.models.User;
import com.secjar.secjarapi.services.UserService;
import dev.samstevens.totp.code.CodeVerifier;
import dev.samstevens.totp.code.DefaultCodeGenerator;
import dev.samstevens.totp.code.DefaultCodeVerifier;
import dev.samstevens.totp.time.SystemTimeProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;


public class CustomAuthenticationProvider extends DaoAuthenticationProvider {

    private final UserService userService;

    private final CodeVerifier mfaVerifier;

    public CustomAuthenticationProvider(UserService userService) {
        this.userService = userService;

        this.mfaVerifier = new DefaultCodeVerifier(new DefaultCodeGenerator(), new SystemTimeProvider());
    }


    @Override
    public Authentication authenticate(Authentication authentication) {

        String verificationCode = ((CustomWebAuthenticationDetails) authentication.getDetails()).getToken();
        User user = userService.getUserByUsername(authentication.getName());

        if ((user == null)) {
            throw new BadCredentialsException("Invalid username or password");
        }

        if (user.isUsingMFA()) {
            if (!isValidLong(verificationCode) || !mfaVerifier.isValidCode(user.getMFASecret(), verificationCode)) {
                throw new BadCredentialsException("Invalid 2fa code");
            }
        }

        if (!user.isVerified()) {
            throw new EmailNotVerifiedException("Email is not verified");
        }

        Authentication result = super.authenticate(authentication);
        return new UsernamePasswordAuthenticationToken(user, result.getCredentials(), result.getAuthorities());
    }

    private boolean isValidLong(String code) {
        try {
            Long.parseLong(code);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }
}
