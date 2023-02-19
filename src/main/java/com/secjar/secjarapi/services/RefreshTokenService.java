package com.secjar.secjarapi.services;

import com.secjar.secjarapi.exceptions.ResourceNotFoundException;
import com.secjar.secjarapi.models.RefreshToken;
import com.secjar.secjarapi.models.User;
import com.secjar.secjarapi.repositories.RefreshTokenRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }


    public void saveRefreshTokenToken(RefreshToken token) {
        refreshTokenRepository.save(token);
    }

    public RefreshToken getRefreshTokenByToken(String token) {
        return refreshTokenRepository.findByToken(token).orElseThrow(() -> new ResourceNotFoundException(String.format("Refresh token %s not found", token)));
    }

    public void deleteRefreshToken(RefreshToken refreshToken) {
        refreshTokenRepository.delete(refreshToken);
    }

    public String generateRefreshToken(User user) {
        RefreshToken refreshToken = new RefreshToken(UUID.randomUUID().toString(), LocalDateTime.now(), LocalDateTime.now().plusMonths(1), user);

        saveRefreshTokenToken(refreshToken);

        return refreshToken.getToken();
    }

    public boolean isRefreshTokenExpired(RefreshToken refreshToken) {
        return refreshToken.getExpiresAt().isAfter(LocalDateTime.now());
    }
}
