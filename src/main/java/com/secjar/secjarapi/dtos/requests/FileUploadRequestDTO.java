package com.secjar.secjarapi.dtos.requests;

import lombok.NonNull;
import org.springframework.web.multipart.MultipartFile;

public record FileUploadRequestDTO(@NonNull MultipartFile file, @NonNull boolean replace, String parentDirectoryUuid) {
}
