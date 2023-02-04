package com.secjar.secjarapi.dtos.requests;

public record ChangePasswordRequestDTO(String currentPassword, String newPassword) {
}
