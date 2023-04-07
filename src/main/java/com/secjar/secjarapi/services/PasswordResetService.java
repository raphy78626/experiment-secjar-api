package com.secjar.secjarapi.services;

import com.secjar.secjarapi.dtos.requests.PasswordResetConfirmRequestDTO;
import com.secjar.secjarapi.models.PasswordResetToken;
import com.secjar.secjarapi.models.User;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class PasswordResetService {

    private final UserService userService;
    private final PasswordResetTokenService passwordResetTokenService;
    private final EmailSenderService emailSenderService;

    public PasswordResetService(UserService userService, PasswordResetTokenService passwordResetTokenService, EmailSenderService emailSenderService) {
        this.userService = userService;
        this.passwordResetTokenService = passwordResetTokenService;
        this.emailSenderService = emailSenderService;
    }

    public String sendPasswordResetToken(String userEmail) {

        User user = userService.getUserByEmail(userEmail);

        PasswordResetToken passwordResetToken = new PasswordResetToken(
                UUID.randomUUID().toString(),
                LocalDateTime.now(),
                LocalDateTime.now().plusMinutes(20),
                user
        );

        passwordResetTokenService.savePasswordResetToken(passwordResetToken);

        emailSenderService.sendSimpleMail(user.getEmail(), "Password reset link: ", "http://localhost:5173/passwordReset?token=" + passwordResetToken.getToken());

        return user.getUuid();
    }

    public void confirmPasswordResetToken(PasswordResetConfirmRequestDTO passwordResetConfirmRequestDTO) {
        PasswordResetToken passwordResetToken = passwordResetTokenService.getToken(passwordResetConfirmRequestDTO.token());

        if (passwordResetToken.getUsedAt() != null) {
            throw new IllegalStateException("Link was already used");
        }

        if (passwordResetToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("Password reset token expired");
        }

        userService.changeUserPasswordByEmail(passwordResetToken.getUser().getEmail(), passwordResetConfirmRequestDTO.newPassword());

        passwordResetTokenService.setUsedAt(passwordResetConfirmRequestDTO.token());
    }
}
