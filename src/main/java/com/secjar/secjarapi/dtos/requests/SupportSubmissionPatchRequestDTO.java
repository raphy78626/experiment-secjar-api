package com.secjar.secjarapi.dtos.requests;

import com.secjar.secjarapi.enums.SupportSubmissionStatesEnum;

public record SupportSubmissionPatchRequestDTO(SupportSubmissionStatesEnum submissionStatus) {
}
