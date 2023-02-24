package com.secjar.secjarapi.dtos.responses;

import com.secjar.secjarapi.models.SupportSubmissionNote;

import java.util.List;

public record SupportSubmissionNotesResponseDTO(List<SupportSubmissionNote> supportSubmissionNotes) {
}
