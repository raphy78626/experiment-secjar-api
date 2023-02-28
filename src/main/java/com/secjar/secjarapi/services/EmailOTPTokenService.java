package com.secjar.secjarapi.services;

import com.secjar.secjarapi.models.EmailOTPToken;
import com.secjar.secjarapi.models.User;
import com.secjar.secjarapi.repositories.EmailOTPTokenRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class EmailOTPTokenService {

    private final EmailOTPTokenRepository emailOTPTOkenRepository;

    public EmailOTPTokenService(EmailOTPTokenRepository emailOTPTOkenRepository) {
        this.emailOTPTOkenRepository = emailOTPTOkenRepository;
    }


    public void saveEmailOTPToken(EmailOTPToken token) {
        emailOTPTOkenRepository.save(token);
    }

    public void deleteEmailOTPToken(EmailOTPToken emailOTPToken) {
        emailOTPTOkenRepository.delete(emailOTPToken);
    }

    public void deleteEmailOTPTokenByToken(String token) {
        emailOTPTOkenRepository.deleteByToken(token);
    }

    public String generateEmailOTPToken(User user) {
        EmailOTPToken refreshToken = new EmailOTPToken(UUID.randomUUID().toString(), LocalDateTime.now(), LocalDateTime.now().plusMinutes(10), user);
        saveEmailOTPToken(refreshToken);

        return refreshToken.getToken();
    }

    public boolean isEmailOTPTokenExpired(EmailOTPToken emailOTPToken) {
        return emailOTPToken.getExpiresAt().isAfter(LocalDateTime.now());
    }
}
