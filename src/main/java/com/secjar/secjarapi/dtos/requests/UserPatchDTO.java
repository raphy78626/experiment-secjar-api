package com.secjar.secjarapi.dtos.requests;

import com.secjar.secjarapi.constrains.UserConstrains;

public record UserPatchDTO(Long fileDeletionDelay, Long desiredSessionTime) implements UserConstrains {

    public UserPatchDTO {
        if (fileDeletionDelay != null) {
            if (fileDeletionDelay < MIN_FILE_DELETION_DELAY) {
                throw new IllegalArgumentException(String.format("FileDeletionDelay cannot be smaller than %d ms", MIN_FILE_DELETION_DELAY));
            }
            if (fileDeletionDelay > MAX_FILE_DELETION_DELAY) {
                throw new IllegalArgumentException(String.format("FileDeletionDelay cannot be larger than %d ms", MAX_FILE_DELETION_DELAY));
            }
        }

        if(desiredSessionTime != null) {
            if(desiredSessionTime < MIN_SESSION_TIME) {
                throw new IllegalArgumentException(String.format("FileDeletionDelay cannot be smaller than %d ms", MIN_SESSION_TIME));
            }
            if(desiredSessionTime > MAX_SESSION_TIME) {
                throw new IllegalArgumentException(String.format("FileDeletionDelay cannot be larger than %d ms", MAX_SESSION_TIME));
            }
        }
    }
}
