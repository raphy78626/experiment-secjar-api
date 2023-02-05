package com.secjar.secjarapi.dtos.requests;

import org.springframework.web.multipart.MultipartFile;

public record FileUploadRequestDTO(MultipartFile file, String parentDirectoryUuid) {
}
