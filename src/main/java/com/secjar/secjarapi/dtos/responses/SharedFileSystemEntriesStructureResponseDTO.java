package com.secjar.secjarapi.dtos.responses;

import com.secjar.secjarapi.models.FileSystemEntryInfo;

import java.util.List;

public record SharedFileSystemEntriesStructureResponseDTO(List<FileSystemEntryInfo> fileSystemEntryInfoList) {
}
