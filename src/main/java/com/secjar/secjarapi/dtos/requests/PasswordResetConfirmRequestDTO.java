package com.secjar.secjarapi.dtos.requests;

public record PasswordResetConfirmRequestDTO(String token, String userEmail, String newPassword) {
}
