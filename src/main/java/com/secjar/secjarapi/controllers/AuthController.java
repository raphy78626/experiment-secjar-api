package com.secjar.secjarapi.controllers;

import com.secjar.secjarapi.config.CustomWebAuthenticationDetails;
import com.secjar.secjarapi.dtos.requests.LoginRequestDTO;
import com.secjar.secjarapi.dtos.requests.RefreshTokenLoginRequestDTO;
import com.secjar.secjarapi.dtos.requests.Send2FATokenIfEnabledRequestDTO;
import com.secjar.secjarapi.dtos.responses.LoginResponseDTO;
import com.secjar.secjarapi.dtos.responses.MessageResponseDTO;
import com.secjar.secjarapi.enums.MFATypeEnum;
import com.secjar.secjarapi.models.RefreshToken;
import com.secjar.secjarapi.models.User;
import com.secjar.secjarapi.services.JwtTokenService;
import com.secjar.secjarapi.services.MFAService;
import com.secjar.secjarapi.services.RefreshTokenService;
import com.secjar.secjarapi.services.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {

    private final JwtTokenService jwtTokenService;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenService refreshTokenService;
    private final UserService userService;
    private final MFAService mfaService;

    public AuthController(JwtTokenService jwtTokenService, AuthenticationManager authenticationManager, RefreshTokenService refreshTokenService, UserService userService, MFAService mfaService) {
        this.jwtTokenService = jwtTokenService;
        this.authenticationManager = authenticationManager;
        this.refreshTokenService = refreshTokenService;
        this.userService = userService;
        this.mfaService = mfaService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> token(@RequestBody LoginRequestDTO userLogin, HttpServletRequest request) throws AuthenticationException {

        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(userLogin.username(), userLogin.password());

        usernamePasswordAuthenticationToken.setDetails(new CustomWebAuthenticationDetails(request, userLogin.mfaToken()));

        Authentication authentication = authenticationManager.authenticate(usernamePasswordAuthenticationToken);

        return ResponseEntity.ok(new LoginResponseDTO(jwtTokenService.generateToken((User) authentication.getPrincipal()), refreshTokenService.generateRefreshToken((User) authentication.getPrincipal())));
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> loginWithRefreshToken(@RequestBody RefreshTokenLoginRequestDTO refreshTokenLoginRequestDTO, HttpServletRequest request) {

        RefreshToken refreshToken = refreshTokenService.getRefreshTokenByToken(refreshTokenLoginRequestDTO.refreshToken());
        boolean isRefreshTokenValid = refreshTokenService.isRefreshTokenExpired(refreshToken);
        refreshTokenService.deleteRefreshToken(refreshToken);

        if (isRefreshTokenValid) {
            return ResponseEntity.ok(new LoginResponseDTO(jwtTokenService.generateToken(refreshToken.getUser()), refreshTokenService.generateRefreshToken(refreshToken.getUser())));
        } else {
            return ResponseEntity.ok(new MessageResponseDTO("Invalid refresh token"));
        }
    }

    @PostMapping("/send2FATokenIfEnabled")
    public ResponseEntity<MessageResponseDTO> send2FATokenIfEnabled(@RequestBody Send2FATokenIfEnabledRequestDTO send2FATokenIfEnabledRequestDTO) {
        User user = userService.getUserByUsername(send2FATokenIfEnabledRequestDTO.username());

        if (user.getMfaType() == MFATypeEnum.OTP_EMAIL) {
            mfaService.sendEmailOTP(user);
        }

        return ResponseEntity.ok(new MessageResponseDTO("2FA token send"));
    }
}
