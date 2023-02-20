package com.secjar.secjarapi.services;

import com.secjar.secjarapi.dtos.requests.RegistrationRequestDTO;
import com.secjar.secjarapi.enums.UserRolesEnum;
import com.secjar.secjarapi.exceptions.BadNewPasswordException;
import com.secjar.secjarapi.models.AccountCreationCredentials;
import com.secjar.secjarapi.models.ConfirmationToken;
import com.secjar.secjarapi.models.User;
import com.secjar.secjarapi.models.UserRole;
import com.secjar.secjarapi.utils.PasswordValidatorUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Service
public class RegistrationService {

    @Value("${accountCreation.emailConfirmationSiteAddress}")
    private String emailConfirmationSite;

    private final UserService userService;
    private final ConfirmationTokenService confirmationTokenService;
    private final EmailSenderService emailSenderService;
    private final AccountCreationCredentialsService accountCreationCredentialsService;
    private final RoleService roleService;
    private final PasswordEncoder passwordEncoder;
    private final PasswordValidatorUtil passwordValidator;

    public RegistrationService(UserService userService, ConfirmationTokenService confirmationTokenService, EmailSenderService emailSenderService, AccountCreationCredentialsService accountCreationCredentialsService, RoleService roleService, PasswordEncoder passwordEncoder, PasswordValidatorUtil passwordValidator) {
        this.userService = userService;
        this.confirmationTokenService = confirmationTokenService;
        this.emailSenderService = emailSenderService;
        this.accountCreationCredentialsService = accountCreationCredentialsService;
        this.roleService = roleService;
        this.passwordEncoder = passwordEncoder;
        this.passwordValidator = passwordValidator;
    }

    public void register(String accountCreationToken, RegistrationRequestDTO registrationRequestDTO) {

        accountCreationCredentialsService.setUsedAt(accountCreationToken);

        AccountCreationCredentials accountCreationCredentials = accountCreationCredentialsService.getTokenByToken(accountCreationToken);

        UserRole userRole = roleService.getRole(UserRolesEnum.ROLE_USER);


        if (!passwordValidator.validate(registrationRequestDTO.password())) {
            throw new BadNewPasswordException();
        }

        User user = new User(
                UUID.randomUUID().toString(),
                accountCreationCredentials.getUsername(),
                accountCreationCredentials.getName(),
                accountCreationCredentials.getSurname(),
                passwordEncoder.encode(registrationRequestDTO.password()),
                accountCreationCredentials.getEmail(),
                Set.of(userRole));

        userService.saveUser(user);

        ConfirmationToken confirmationToken = new ConfirmationToken(
                UUID.randomUUID().toString(),
                LocalDateTime.now(),
                LocalDateTime.now().plusMinutes(20),
                user
        );

        confirmationTokenService.saveConfirmationToken(confirmationToken);

        emailSenderService.sendSimpleMail(user.getEmail(), "Confirm your account", emailConfirmationSite + "?token=" + confirmationToken.getToken());
    }

    public void confirmRegistrationToken(String token) {
        ConfirmationToken confirmationToken = confirmationTokenService.getTokenByToken(token);

        if (confirmationToken.getConfirmedAt() != null) {
            throw new IllegalStateException("Account already confirmed");
        }

        if (confirmationToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("Confirmation token expired");
        }

        confirmationTokenService.setConfirmedAt(token);
        userService.enableUser(confirmationToken.getUser());
        userService.addCryptoKeyToUser(confirmationToken.getUser().getId());
    }
}
