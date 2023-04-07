package com.secjar.secjarapi.services;

import com.secjar.secjarapi.dtos.requests.InviteUserRequestDTO;
import com.secjar.secjarapi.models.AccountCreationCredentials;
import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class UserInvitationService {

    @Value("${frontendUrls.accountActivationPageUrl}")
    private String accountCreationSite;

    private final EmailSenderService emailSenderService;
    private final UserService userService;
    private final AccountCreationCredentialsService accountCreationCredentialsService;

    public UserInvitationService(EmailSenderService emailSenderService, UserService userService, AccountCreationCredentialsService accountCreationCredentialsService) {
        this.emailSenderService = emailSenderService;
        this.userService = userService;
        this.accountCreationCredentialsService = accountCreationCredentialsService;
    }

    public void inviteUser(InviteUserRequestDTO inviteUserRequestDTO) {
        boolean isValidEmail = EmailValidator.getInstance().isValid(inviteUserRequestDTO.email());

        if (!isValidEmail) {
            throw new IllegalStateException("Email is not valid");
        }

        if (userService.checkIfUserWithEmailExist(inviteUserRequestDTO.email())) {
            throw new IllegalStateException("Email is already taken");
        }

        if (userService.checkIfUserWithUsernameExist(inviteUserRequestDTO.username())) {
            throw new IllegalStateException("Username is already taken");
        }

        AccountCreationCredentials accountCreationCredentials = new AccountCreationCredentials(
                inviteUserRequestDTO.username(),
                inviteUserRequestDTO.name(),
                inviteUserRequestDTO.surname(),
                inviteUserRequestDTO.email(),
                UUID.randomUUID().toString(),
                LocalDateTime.now(),
                LocalDateTime.now().plusMinutes(20)
        );

        accountCreationCredentialsService.saveAccountCreationToken(accountCreationCredentials);

        emailSenderService.sendSimpleMail(inviteUserRequestDTO.email(), "Create your account", accountCreationSite + "?token=" + accountCreationCredentials.getToken());
    }
}
