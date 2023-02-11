package com.secjar.secjarapi.dtos.responses;

import java.util.Set;

public record DiskInfoResponseDTO(Set<String> disallowedMimeTypes) {
}
