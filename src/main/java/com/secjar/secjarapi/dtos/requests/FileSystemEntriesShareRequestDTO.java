package com.secjar.secjarapi.dtos.requests;

import com.secjar.secjarapi.enums.ShareActionsEnum;
import com.secjar.secjarapi.enums.ShareTypesEnum;
import lombok.NonNull;

import java.util.List;

public record FileSystemEntriesShareRequestDTO(@NonNull List<String> fileSystemEntriesUuid, List<String> usersUuids, @NonNull ShareTypesEnum shareType, @NonNull ShareActionsEnum shareAction) {
}
