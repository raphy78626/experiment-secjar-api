package com.secjar.secjarapi.services;

import com.secjar.secjarapi.exceptions.ResourceNotFoundException;
import com.secjar.secjarapi.models.AccountCreationCredentials;
import com.secjar.secjarapi.repositories.AccountCreationCredentialsRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AccountCreationCredentialsService {

    private final AccountCreationCredentialsRepository accountCreationCredentialsRepository;

    public AccountCreationCredentialsService(AccountCreationCredentialsRepository accountCreationCredentialsRepository) {
        this.accountCreationCredentialsRepository = accountCreationCredentialsRepository;
    }

    public void saveAccountCreationToken(AccountCreationCredentials token) {
        accountCreationCredentialsRepository.save(token);
    }

    public AccountCreationCredentials getTokenByToken(String token) {
        return accountCreationCredentialsRepository.findByToken(token).orElseThrow(() -> new ResourceNotFoundException(String.format("Account creation token %s not found", token)));
    }

    public void setUsedAt(String token) {
        AccountCreationCredentials confirmationToken = getTokenByToken(token);

        confirmationToken.setUsedAt(LocalDateTime.now());

        accountCreationCredentialsRepository.save(confirmationToken);
    }
}
