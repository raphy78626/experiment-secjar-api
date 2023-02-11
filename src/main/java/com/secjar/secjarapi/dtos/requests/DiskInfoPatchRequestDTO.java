package com.secjar.secjarapi.dtos.requests;

import com.secjar.secjarapi.constrains.DiskConstrains;

import java.util.Set;

public record DiskInfoPatchRequestDTO(Long maxUserSessionTime, Set<String> disallowedMimeTypes) implements DiskConstrains {

    public DiskInfoPatchRequestDTO {
        if (maxUserSessionTime != null) {
            if (maxUserSessionTime < MIN_MAX_SESSION_TIME) {
                throw new IllegalArgumentException(String.format("Max session time cannot be smaller than %d ms", MIN_MAX_SESSION_TIME));
            }
            if (maxUserSessionTime > MAX_MAX_SESSION_TIME) {
                throw new IllegalArgumentException(String.format("Max session time cannot be larger than %d ms", MAX_MAX_SESSION_TIME));
            }
        }
    }
}
