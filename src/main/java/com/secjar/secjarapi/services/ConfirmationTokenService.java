package com.secjar.secjarapi.services;

import com.secjar.secjarapi.models.ConfirmationToken;
import com.secjar.secjarapi.repositories.ConfirmationTokenRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class ConfirmationTokenService {

    private final ConfirmationTokenRepository confirmationTokenRepository;

    public ConfirmationTokenService(ConfirmationTokenRepository confirmationTokenRepository) {
        this.confirmationTokenRepository = confirmationTokenRepository;
    }

    public void saveConfirmationToken(ConfirmationToken token) {
        confirmationTokenRepository.save(token);
    }

    public Optional<ConfirmationToken> getToken(String token) {
        return confirmationTokenRepository.findByToken(token);
    }

    public void setConfirmedAt(String token) {
        ConfirmationToken confirmationToken = confirmationTokenRepository.findByToken(token).orElseThrow(() -> new RuntimeException(String.format("Token %s not found", token)));

        confirmationToken.setConfirmedAt(LocalDateTime.now());

        confirmationTokenRepository.save(confirmationToken);
    }
}
