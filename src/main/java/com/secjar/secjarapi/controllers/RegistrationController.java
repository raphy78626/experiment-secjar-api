package com.secjar.secjarapi.controllers;

import com.secjar.secjarapi.dtos.RegistrationRequestDTO;
import com.secjar.secjarapi.services.RegistrationService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RegistrationController {

    private final RegistrationService registrationService;

    public RegistrationController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @PostMapping("/register")
    public String register(@RequestBody RegistrationRequestDTO request) {
        return registrationService.register(request);
    }
}
