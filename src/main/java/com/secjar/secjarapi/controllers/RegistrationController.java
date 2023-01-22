package com.secjar.secjarapi.controllers;

import com.secjar.secjarapi.dtos.requests.RegistrationRequestDTO;
import com.secjar.secjarapi.services.RegistrationService;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("/register/confirm")
    public String confirm(@RequestParam("token") String token) {
        registrationService.confirmRegistrationToken(token);
        return "confirmed";
    }
}
