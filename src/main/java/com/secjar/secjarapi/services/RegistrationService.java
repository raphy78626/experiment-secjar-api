package com.secjar.secjarapi.services;

import com.secjar.secjarapi.dtos.requests.RegistrationRequestDTO;
import com.secjar.secjarapi.models.ConfirmationToken;
import com.secjar.secjarapi.models.User;
import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class RegistrationService {

    private final UserService userService;
    private final ConfirmationTokenService confirmationTokenService;
    private final EmailSenderService emailSenderService;

    public RegistrationService(UserService userService, ConfirmationTokenService confirmationTokenService, EmailSenderService emailSenderService) {
        this.userService = userService;
        this.confirmationTokenService = confirmationTokenService;
        this.emailSenderService = emailSenderService;
    }

    public String register(RegistrationRequestDTO registrationRequestDTO) {
        boolean isValidEmail = EmailValidator.getInstance().isValid(registrationRequestDTO.email());

        if (!isValidEmail) {
            throw new IllegalStateException("email not valid");
        }

        if (userService.checkIfUserWithEmailExist(registrationRequestDTO.email())) {
            throw new IllegalStateException("email already taken");
        }

        if (userService.checkIfUserWithUsernameExist(registrationRequestDTO.username())) {
            throw new IllegalStateException("username already taken");
        }

        User user = userService.createUserFromRegistrationRequest(registrationRequestDTO);
        userService.saveUser(user);

        ConfirmationToken confirmationToken = new ConfirmationToken(
                UUID.randomUUID().toString(),
                LocalDateTime.now(),
                LocalDateTime.now().plusMinutes(20),
                user
        );

        confirmationTokenService.saveConfirmationToken(confirmationToken);

        emailSenderService.sendSimpleMail(user.getEmail(), "Confirm your account", "http://localhost:8080/register/confirm?token=" + confirmationToken.getToken());

        return user.getUuid();
    }

    public void confirmRegistrationToken(String token) {
        ConfirmationToken confirmationToken = confirmationTokenService.getToken(token).orElseThrow(() -> new IllegalStateException(String.format("Confirmation token %s not found", token)));

        if (confirmationToken.getConfirmedAt() != null) {
            throw new IllegalStateException("Account already confirmed");
        }

        if (confirmationToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("Confirmation token expired");
        }

        confirmationTokenService.setConfirmedAt(token);
        userService.enableUser(confirmationToken.getUser().getEmail());
        userService.addCryptoKeyToUser(confirmationToken.getUser().getId());
    }
}
