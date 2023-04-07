package com.secjar.secjarapi.dtos.requests;

import lombok.NonNull;

public record PasswordResetConfirmRequestDTO(@NonNull String token, @NonNull String newPassword) {
}
