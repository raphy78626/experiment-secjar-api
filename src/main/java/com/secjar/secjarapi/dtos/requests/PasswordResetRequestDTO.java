package com.secjar.secjarapi.dtos.requests;

import lombok.NonNull;

public record PasswordResetRequestDTO(@NonNull String userEmail) {
}
