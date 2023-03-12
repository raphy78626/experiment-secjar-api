package com.secjar.secjarapi.dtos.requests;

import lombok.NonNull;

public record SupportRequestDTO(@NonNull String name, @NonNull String surname, @NonNull String email, @NonNull String message) {
}
