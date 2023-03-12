package com.secjar.secjarapi.dtos.requests;

import lombok.NonNull;

public record RefreshTokenLoginRequestDTO(@NonNull String refreshToken) {
}
