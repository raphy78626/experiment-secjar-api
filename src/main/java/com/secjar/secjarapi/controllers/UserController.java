package com.secjar.secjarapi.controllers;

import com.secjar.secjarapi.dtos.requests.PasswordResetConfirmRequestDTO;
import com.secjar.secjarapi.dtos.requests.PasswordResetRequestDTO;
import com.secjar.secjarapi.dtos.requests.UserPatchDTO;
import com.secjar.secjarapi.dtos.responses.MessageResponseDTO;
import com.secjar.secjarapi.models.User;
import com.secjar.secjarapi.services.PasswordResetService;
import com.secjar.secjarapi.services.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;
    private final PasswordResetService passwordResetService;

    public UserController(UserService userService, PasswordResetService passwordResetService) {
        this.userService = userService;
        this.passwordResetService = passwordResetService;
    }

    @PatchMapping
    public ResponseEntity<MessageResponseDTO> patchUser(@RequestBody UserPatchDTO userPatchDTO, @AuthenticationPrincipal Jwt principal) {
        User user = getUserFromPrincipal(principal);

        userService.pathUser(user.getUuid(), userPatchDTO);

        return ResponseEntity.status(204).build();
    }

    @PostMapping("/changePassword")
    public ResponseEntity<MessageResponseDTO> changeUserPassword(@RequestParam String newPassword, @AuthenticationPrincipal Jwt principal) {

        User user = getUserFromPrincipal(principal);

        userService.changeUserPasswordByUuid(user.getUuid(), newPassword);

        return ResponseEntity.ok(new MessageResponseDTO("Password changed"));
    }

    @PostMapping("/passwordReset")
    public ResponseEntity<MessageResponseDTO> sendPasswordResetLink(@RequestBody PasswordResetRequestDTO passwordResetRequestDTO) {

        passwordResetService.sendPasswordResetToken(passwordResetRequestDTO.userEmail());

        return ResponseEntity.ok(new MessageResponseDTO("Email with password reset link sent"));
    }

    @PostMapping("/passwordReset/confirm")
    public ResponseEntity<MessageResponseDTO> resetUserPassword(@RequestBody PasswordResetConfirmRequestDTO passwordResetConfirmRequestDTO) {

        passwordResetService.confirmPasswordResetToken(passwordResetConfirmRequestDTO);

        return ResponseEntity.ok(new MessageResponseDTO("Password changed"));
    }

    private User getUserFromPrincipal(Jwt principal) {
        String userUuid = principal.getClaims().get("userUuid").toString();
        return userService.getUserByUuid(userUuid);
    }
}
