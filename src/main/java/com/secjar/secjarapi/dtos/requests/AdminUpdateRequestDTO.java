package com.secjar.secjarapi.dtos.requests;

import lombok.NonNull;

public record AdminUpdateRequestDTO(@NonNull boolean isUserAdmin) {
}
