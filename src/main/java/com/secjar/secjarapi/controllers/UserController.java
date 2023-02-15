package com.secjar.secjarapi.controllers;

import com.secjar.secjarapi.dtos.requests.*;
import com.secjar.secjarapi.dtos.responses.MFAQrCodeResponse;
import com.secjar.secjarapi.dtos.responses.MessageResponseDTO;
import com.secjar.secjarapi.dtos.responses.UserInfoResponseDTO;
import com.secjar.secjarapi.models.User;
import com.secjar.secjarapi.models.UserRole;
import com.secjar.secjarapi.services.FileSystemEntryService;
import com.secjar.secjarapi.services.PasswordResetService;
import com.secjar.secjarapi.services.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final PasswordResetService passwordResetService;
    private final FileSystemEntryService fileSystemEntryService;

    public UserController(UserService userService, PasswordResetService passwordResetService, FileSystemEntryService fileSystemEntryService) {
        this.userService = userService;
        this.passwordResetService = passwordResetService;
        this.fileSystemEntryService = fileSystemEntryService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/info")
    public ResponseEntity<List<UserInfoResponseDTO>> getAllUsersInfo() {
        List<User> users = userService.getAllUsers();

        List<UserInfoResponseDTO> responseUsers = users.stream().map(user ->
                new UserInfoResponseDTO(
                        user.getUsername(),
                        user.getEmail(),
                        user.isVerified(),
                        user.isUsingMFA(),
                        user.getFileDeletionDelay(),
                        user.getCurrentDiskSpace(),
                        user.getAllowedDiskSpace(),
                        user.getFileSystemEntries().size(),
                        user.getRoles().stream().map(UserRole::getRole).toList())
        ).toList();

        return ResponseEntity.ok(responseUsers);
    }

    @GetMapping("/{uuid}/info")
    public ResponseEntity<?> getUserInfo(@PathVariable("uuid") String userUuid, @AuthenticationPrincipal Jwt principal) {
        User user = getUserFromPrincipal(principal);

        if (!user.getUuid().equals(userUuid) && !userService.isUserAdmin(user.getUuid())) {
            return ResponseEntity.status(403).body(new MessageResponseDTO("You can't access this user info"));
        }

        return ResponseEntity.ok().body(new UserInfoResponseDTO(
                user.getUsername(),
                user.getEmail(),
                user.isVerified(),
                user.isUsingMFA(),
                user.getFileDeletionDelay(),
                user.getCurrentDiskSpace(),
                user.getAllowedDiskSpace(),
                user.getFileSystemEntries().size(),
                user.getRoles().stream().map(UserRole::getRole).toList()
        ));
    }

    @PatchMapping("/{uuid}")
    public ResponseEntity<MessageResponseDTO> patchUser(@PathVariable("uuid") String userUuid, @RequestBody UserPatchRequestDTO userPatchRequestDTO, @AuthenticationPrincipal Jwt principal) {

        String userUuidFromPrincipal = getUserUuidFromPrincipal(principal);

        if (!userUuidFromPrincipal.equals(userUuid) && !userService.isUserAdmin(userUuidFromPrincipal)) {
            return ResponseEntity.status(403).body(new MessageResponseDTO("You can't change data of this user"));
        }

        userService.pathUser(userUuid, userPatchRequestDTO);

        return ResponseEntity.status(204).build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{uuid}/roles/admin")
    public ResponseEntity<?> updateUserAdminRole(@PathVariable("uuid") String userUuid, @RequestBody AdminUpdateRequestDTO adminUpdateRequestDTO) {
        userService.updateUserAdminRole(userUuid, adminUpdateRequestDTO.isUserAdmin());

        if (adminUpdateRequestDTO.isUserAdmin()) {
            return ResponseEntity.ok(new MessageResponseDTO(String.format("User %s is not an admin", userUuid)));
        } else {
            return ResponseEntity.ok(new MessageResponseDTO(String.format("User %s is no longer an admin", userUuid)));
        }
    }

    @PostMapping("/{uuid}/2fa/update")
    public ResponseEntity<?> updateUserUsing2FA(@PathVariable("uuid") String userUuid, @RequestBody Update2FARequest update2FARequest, @AuthenticationPrincipal Jwt principal) {

        String userUuidFromPrincipal = getUserUuidFromPrincipal(principal);

        if (!userUuidFromPrincipal.equals(userUuid)) {
            return ResponseEntity.status(403).body(new MessageResponseDTO("You can't change data of this user"));
        }

        userService.updateUserMFA(userUuid, update2FARequest.use2FA());

        if (update2FARequest.use2FA()) {
            return ResponseEntity.ok(new MFAQrCodeResponse(userService.generateQRUrl(userUuid)));
        }

        return ResponseEntity.ok(new MessageResponseDTO("2FA authentication disabled"));
    }

    @PostMapping("/{uuid}/changePassword")
    public ResponseEntity<MessageResponseDTO> changeUserPassword(@PathVariable("uuid") String userUuid, @RequestBody ChangePasswordRequestDTO changePasswordRequestDTO, @AuthenticationPrincipal Jwt principal) {

        if (!userUuid.equals(getUserUuidFromPrincipal(principal))) {
            return ResponseEntity.status(403).body(new MessageResponseDTO("You can't change data of this user"));
        }

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

    @DeleteMapping("/{uuid}")
    public ResponseEntity<MessageResponseDTO> deleteUser(@PathVariable("uuid") String userToDeleteUuid, @AuthenticationPrincipal Jwt principal) {
        User user = getUserFromPrincipal(principal);

        if (!user.getUuid().equals(userToDeleteUuid) && !userService.isUserAdmin(user.getUuid())) {
            return ResponseEntity.status(403).body(new MessageResponseDTO("You can't delete this user"));
        }

        fileSystemEntryService.deleteAllUserFileSystemEntries(user);
        userService.deleteUserByUuid(userToDeleteUuid);

        return ResponseEntity.ok(new MessageResponseDTO("User deleted"));
    }

    private User getUserFromPrincipal(Jwt principal) {
        String userUuid = principal.getClaims().get("userUuid").toString();
        return userService.getUserByUuid(userUuid);
    }

    private String getUserUuidFromPrincipal(Jwt principal) {
        return principal.getClaims().get("userUuid").toString();
    }
}
