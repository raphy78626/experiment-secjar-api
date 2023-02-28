package com.secjar.secjarapi.dtos.requests;

import lombok.NonNull;

public record Send2FATokenIfEnabledRequestDTO(@NonNull String username) {
}
