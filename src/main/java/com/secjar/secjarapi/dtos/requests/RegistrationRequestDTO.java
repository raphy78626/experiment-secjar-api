package com.secjar.secjarapi.dtos.requests;

import lombok.NonNull;

public record RegistrationRequestDTO(@NonNull String accountCreationToken, @NonNull String password) {
}
