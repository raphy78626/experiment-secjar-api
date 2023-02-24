package com.secjar.secjarapi.services;

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
}
