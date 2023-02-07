package com.secjar.secjarapi.dtos.responses;

import com.secjar.secjarapi.enums.UserRolesEnum;

import java.util.List;

public record UserInfoResponseDTO(
        String username,
        String email,
        boolean isVerified,
        boolean isUsing2FA,
        long fileDeletionDelay,
        long currentDiscSpace,
        long allowedDiscSpace,
        int fileSystemEntriesNumber,
        List<UserRolesEnum> roles) {
}
