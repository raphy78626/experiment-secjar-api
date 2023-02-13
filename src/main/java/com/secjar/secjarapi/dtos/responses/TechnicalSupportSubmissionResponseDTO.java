package com.secjar.secjarapi.dtos.responses;

import com.secjar.secjarapi.models.SupportSubmission;

import java.util.List;

public record TechnicalSupportSubmissionResponseDTO(List<SupportSubmission> pendingSubmissions) {
}
