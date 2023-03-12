package com.secjar.secjarapi.controllers;

import com.secjar.secjarapi.dtos.requests.InviteUserRequestDTO;
import com.secjar.secjarapi.dtos.requests.RegistrationRequestDTO;
import com.secjar.secjarapi.dtos.responses.MessageResponseDTO;
import com.secjar.secjarapi.services.RegistrationService;
import com.secjar.secjarapi.services.UserInvitationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserInvitationController {

    private final RegistrationService registrationService;
    private final UserInvitationService userInvitationService;

    public UserInvitationController(RegistrationService registrationService, UserInvitationService userInvitationService) {
        this.registrationService = registrationService;
        this.userInvitationService = userInvitationService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/invite")
    public ResponseEntity<MessageResponseDTO> inviteUser(@RequestBody InviteUserRequestDTO inviteUserRequestDTO) {
        userInvitationService.inviteUser(inviteUserRequestDTO);

        return ResponseEntity.ok(new MessageResponseDTO("Invitation email send"));
    }

    @PostMapping("/register")
    public ResponseEntity<MessageResponseDTO> register(@RequestBody RegistrationRequestDTO registrationRequestDTO) {
        registrationService.register(registrationRequestDTO);

        return ResponseEntity.ok(new MessageResponseDTO("Account created"));
    }

    @PostMapping("/register/confirm")
    public String confirm(@RequestParam("token") String token) {
        registrationService.confirmRegistrationToken(token);
        return "confirmed";
    }
}
