package com.secjar.secjarapi.services;

import com.secjar.secjarapi.models.SupportSubmission;
import com.secjar.secjarapi.repositories.SupportSubmissionRepository;
import org.springframework.stereotype.Service;

@Service
public class SupportSubmissionService {

    private final SupportSubmissionRepository supportSubmissionRepository;

    public SupportSubmissionService(SupportSubmissionRepository supportSubmissionRepository) {
        this.supportSubmissionRepository = supportSubmissionRepository;
    }

    public void saveTechnicalSupportSubmission(SupportSubmission supportSubmission) {
        supportSubmissionRepository.save(supportSubmission);
    }
}
