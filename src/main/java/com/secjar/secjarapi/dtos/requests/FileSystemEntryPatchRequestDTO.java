package com.secjar.secjarapi.dtos.requests;

public record FileSystemEntryPatchRequestDTO(String name, Boolean isFavourite, String parentDirectoryUuid) {
}
