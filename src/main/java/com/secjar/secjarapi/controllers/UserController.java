package com.secjar.secjarapi.controllers;

import com.secjar.secjarapi.dtos.requests.*;
import com.secjar.secjarapi.dtos.responses.MFAQrCodeResponse;
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
    public ResponseEntity<MessageResponseDTO> patchUser(@RequestBody UserPatchRequestDTO userPatchRequestDTO, @AuthenticationPrincipal Jwt principal) {
        User user = getUserFromPrincipal(principal);

        userService.pathUser(user.getUuid(), userPatchRequestDTO);

        return ResponseEntity.status(204).build();
    }

    @PostMapping("/changePassword")
    public ResponseEntity<MessageResponseDTO> changeUserPassword(@RequestBody ChangePasswordRequestDTO changePasswordRequestDTO, @AuthenticationPrincipal Jwt principal) {

        String userUuid = getUserUuidFromPrincipal(principal);

        if (!userService.verifyUserPassword(userUuid, changePasswordRequestDTO.currentPassword())) {
            return ResponseEntity.badRequest().body(new MessageResponseDTO("Your current password is wrong"));
        }

        userService.changeUserPasswordByUuid(userUuid, changePasswordRequestDTO.newPassword());

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

    @PostMapping("2fa/update")
    public ResponseEntity<?> updateUserUsing2FA(@RequestBody Update2FARequest update2FARequest, @AuthenticationPrincipal Jwt principal) {

        User user = getUserFromPrincipal(principal);

        userService.updateUserMFA(user.getUuid(), update2FARequest.use2FA());

        if (update2FARequest.use2FA()) {
            return ResponseEntity.ok(new MFAQrCodeResponse(userService.generateQRUrl(user)));
        }

        return ResponseEntity.ok(new MessageResponseDTO("2FA authentication disabled"));
    }

    private User getUserFromPrincipal(Jwt principal) {
        String userUuid = principal.getClaims().get("userUuid").toString();
        return userService.getUserByUuid(userUuid);
    }

    private String getUserUuidFromPrincipal(Jwt principal) {
        return principal.getClaims().get("userUuid").toString();
    }
}
