package com.secjar.secjarapi.dtos.requests;

import java.util.Set;

public record DiskInfoPatchRequestDTO(Set<String> disallowedMimeTypes) {
}
