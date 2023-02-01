package com.secjar.secjarapi.services;

import com.secjar.secjarapi.models.PasswordResetToken;
import com.secjar.secjarapi.repositories.PasswordResetTokenRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;


@Service
public class PasswordResetTokenService {

    private final PasswordResetTokenRepository passwordResetTokenRepository;

    public PasswordResetTokenService(PasswordResetTokenRepository passwordResetTokenRepository) {
        this.passwordResetTokenRepository = passwordResetTokenRepository;
    }

    public void savePasswordResetToken(PasswordResetToken passwordResetToken) {
        passwordResetTokenRepository.save(passwordResetToken);
    }

    public PasswordResetToken getToken(String token) {
        return passwordResetTokenRepository.findByToken(token).orElseThrow(() -> new RuntimeException(String.format("Token: %s does not exist", token)));
    }

    public void setUsedAt(String token) {
        PasswordResetToken passwordResetToken = getToken(token);

        passwordResetToken.setUsedAt(LocalDateTime.now());

        passwordResetTokenRepository.save(passwordResetToken);
    }
}
