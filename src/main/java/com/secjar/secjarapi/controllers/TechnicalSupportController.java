package com.secjar.secjarapi.controllers;

import com.secjar.secjarapi.dtos.requests.SupportRequestDTO;
import com.secjar.secjarapi.dtos.requests.SupportSubmissionPatchRequestDTO;
import com.secjar.secjarapi.dtos.responses.MessageResponseDTO;
import com.secjar.secjarapi.dtos.responses.TechnicalSupportSubmissionResponseDTO;
import com.secjar.secjarapi.models.SupportSubmission;
import com.secjar.secjarapi.services.SupportSubmissionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/support")
public class TechnicalSupportController {

    private final SupportSubmissionService supportSubmissionService;

    public TechnicalSupportController(SupportSubmissionService supportSubmissionService) {
        this.supportSubmissionService = supportSubmissionService;
    }

    @PostMapping("/submissions")
    public ResponseEntity<MessageResponseDTO> createNewTechnicalSupportSubmission(@RequestBody SupportRequestDTO supportRequestDTO) {

        SupportSubmission supportSubmission = new SupportSubmission(
                UUID.randomUUID().toString(),
                supportRequestDTO.name(),
                supportRequestDTO.surname(),
                supportRequestDTO.message());

        supportSubmissionService.saveSupportSubmission(supportSubmission);

        return ResponseEntity.status(201).body(new MessageResponseDTO("Technical support submission created"));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/submissions/pending")
    public ResponseEntity<TechnicalSupportSubmissionResponseDTO> getPendingSubmissions() {
        List<SupportSubmission> pendingSubmissions = supportSubmissionService.getPendingSubmissions();

        return ResponseEntity.ok(new TechnicalSupportSubmissionResponseDTO(pendingSubmissions));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/submissions/{uuid}")
    public ResponseEntity<MessageResponseDTO> patchSubmission(@PathVariable("uuid") String supportSubmissionUuid, @RequestBody SupportSubmissionPatchRequestDTO supportSubmissionPatchRequestDTO) {
        supportSubmissionService.patchSupportSubmission(supportSubmissionUuid, supportSubmissionPatchRequestDTO);
        return ResponseEntity.status(204).build();
    }
}
