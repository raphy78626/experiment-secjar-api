package com.secjar.secjarapi.dtos.requests;

import com.secjar.secjarapi.enums.ShareActionsEnum;
import lombok.NonNull;

import java.util.List;

public record FileSystemEntriesShareRequestDTO(@NonNull List<String> fileSystemEntriesUuid, @NonNull List<String> usersUuids, @NonNull ShareActionsEnum action) {
}
