package com.secjar.secjarapi.services;

import com.secjar.secjarapi.dtos.requests.SupportSubmissionNotePatchRequestDTO;
import com.secjar.secjarapi.exceptions.ResourceNotFoundException;
import com.secjar.secjarapi.models.SupportSubmissionNote;
import com.secjar.secjarapi.repositories.SupportSubmissionNoteRepository;
import org.springframework.stereotype.Service;

@Service
public class SupportSubmissionNoteService {

    private final SupportSubmissionNoteRepository supportSubmissionNoteRepository;

    public SupportSubmissionNoteService(SupportSubmissionNoteRepository supportSubmissionNoteRepository) {
        this.supportSubmissionNoteRepository = supportSubmissionNoteRepository;
    }

    public void saveSupportSubmissionNote(SupportSubmissionNote supportSubmissionNote) {
        supportSubmissionNoteRepository.save(supportSubmissionNote);
    }

    public void deleteSupportSubmissionNote(String noteUuid) {
        supportSubmissionNoteRepository.deleteByUuid(noteUuid);
    }

    public SupportSubmissionNote getSupportSubmissionNoteByUuid(String supportSubmissionNoteUuid) {
        return supportSubmissionNoteRepository.findByUuid(supportSubmissionNoteUuid).orElseThrow(() -> new ResourceNotFoundException(String.format("Support submission note with uuid: %s not found", supportSubmissionNoteUuid)));
    }

    public void pathSupportSubmissionNote(String noteUuid, SupportSubmissionNotePatchRequestDTO supportSubmissionNotePatchRequestDTO) {
        SupportSubmissionNote supportSubmissionNote = getSupportSubmissionNoteByUuid(noteUuid);

        if (supportSubmissionNotePatchRequestDTO.content() != null) {
            supportSubmissionNote.setNoteContent(supportSubmissionNotePatchRequestDTO.content());
        }

        saveSupportSubmissionNote(supportSubmissionNote);
    }
}
