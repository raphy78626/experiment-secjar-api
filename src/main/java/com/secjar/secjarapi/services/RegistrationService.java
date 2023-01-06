package com.secjar.secjarapi.services;

import com.secjar.secjarapi.dtos.RegistrationRequestDTO;
import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.stereotype.Service;

@Service
public class RegistrationService {

    private final UserService userService;

    public RegistrationService(UserService userService) {
        this.userService = userService;
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

        return userService.addUserToDatabase(registrationRequestDTO);
    }
}
