package com.secjar.secjarapi.services;

import com.secjar.secjarapi.enums.SupportSubmissionStatesEnum;
import com.secjar.secjarapi.models.SupportSubmission;
import com.secjar.secjarapi.repositories.SupportSubmissionRepository;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SupportSubmissionService {

    private final SupportSubmissionRepository supportSubmissionRepository;

    public SupportSubmissionService(SupportSubmissionRepository supportSubmissionRepository) {
        this.supportSubmissionRepository = supportSubmissionRepository;
    }

    public void saveTechnicalSupportSubmission(SupportSubmission supportSubmission) {
        supportSubmissionRepository.save(supportSubmission);
    }

    public List<SupportSubmission> getPendingSubmissions() {
        List<Optional<SupportSubmission>> pendingSubmissions = supportSubmissionRepository.findAllByState(SupportSubmissionStatesEnum.PENDING);

        if (pendingSubmissions.isEmpty()) {
            return Collections.emptyList();
        }

        return pendingSubmissions.stream().flatMap(Optional::stream).collect(Collectors.toList());
    }
}
