package com.secjar.secjarapi.dtos.requests;

import lombok.NonNull;

public record InviteUserRequestDTO(@NonNull String username, @NonNull String name, @NonNull String surname,
                                   @NonNull String email) {
}
