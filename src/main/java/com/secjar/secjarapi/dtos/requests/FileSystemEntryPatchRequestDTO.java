package com.secjar.secjarapi.dtos.requests;

public record FileSystemEntryPatchRequestDTO(String name, Boolean isFavorite, String parentDirectoryUuid) {
}
