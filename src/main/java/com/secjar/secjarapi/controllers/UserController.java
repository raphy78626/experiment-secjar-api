package com.secjar.secjarapi.controllers;

import com.secjar.secjarapi.dtos.responses.MessageResponseDTO;
import com.secjar.secjarapi.models.User;
import com.secjar.secjarapi.services.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PatchMapping("/sessionTime")
    public ResponseEntity<MessageResponseDTO> changeUserSessionTime(@RequestParam long desiredSessionTime, @AuthenticationPrincipal Jwt principal) {

        String userUuid = principal.getClaims().get("userUuid").toString();
        User user = userService.getUserByUuid(userUuid);

        user.setDesiredSessionTime(desiredSessionTime);

        userService.saveUser(user);

        return ResponseEntity.ok(new MessageResponseDTO("Desired session time changed"));
    }
}
