package com.secjar.secjarapi.dtos.requests;

import com.secjar.secjarapi.enums.MFATypeEnum;
import lombok.NonNull;

public record Update2FARequest(@NonNull MFATypeEnum mfaType) {
}
