package com.secjar.secjarapi.dtos.responses;

import com.secjar.secjarapi.models.FileInfo;

import java.util.List;

public record AllFilesInfoResponseDTO(List<FileInfo> fileInfoList) {
}
