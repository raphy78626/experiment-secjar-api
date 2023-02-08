package com.secjar.secjarapi.dtos.requests;

import java.util.List;

public record ShareFilesRequestDTO(List<String> filesToShareUuid, List<String> usersToShareWithUuids) {
}
