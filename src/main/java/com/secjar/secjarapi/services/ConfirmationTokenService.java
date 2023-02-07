package com.secjar.secjarapi.services;

import com.secjar.secjarapi.models.ConfirmationToken;
import com.secjar.secjarapi.repositories.ConfirmationTokenRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ConfirmationTokenService {

    private final ConfirmationTokenRepository confirmationTokenRepository;

    public ConfirmationTokenService(ConfirmationTokenRepository confirmationTokenRepository) {
        this.confirmationTokenRepository = confirmationTokenRepository;
    }

    public void saveConfirmationToken(ConfirmationToken token) {
        confirmationTokenRepository.save(token);
    }

    public ConfirmationToken getTokenByToken(String token) {
        return confirmationTokenRepository.findByToken(token).orElseThrow(() -> new IllegalStateException(String.format("Confirmation token %s not found", token)));
    }

    public void setConfirmedAt(String token) {
        ConfirmationToken confirmationToken = getTokenByToken(token);

        confirmationToken.setConfirmedAt(LocalDateTime.now());

        confirmationTokenRepository.save(confirmationToken);
    }
}
