package com.secjar.secjarapi.controllers;

import com.secjar.secjarapi.dtos.requests.SupportRequestDTO;
import com.secjar.secjarapi.dtos.requests.SupportSubmissionCreateRequestDTO;
import com.secjar.secjarapi.dtos.requests.SupportSubmissionNotePatchRequestDTO;
import com.secjar.secjarapi.dtos.requests.SupportSubmissionPatchRequestDTO;
import com.secjar.secjarapi.dtos.responses.MessageResponseDTO;
import com.secjar.secjarapi.dtos.responses.SupportSubmissionNotesResponseDTO;
import com.secjar.secjarapi.dtos.responses.TechnicalSupportSubmissionResponseDTO;
import com.secjar.secjarapi.models.SupportSubmission;
import com.secjar.secjarapi.services.SupportSubmissionNoteService;
import com.secjar.secjarapi.services.SupportSubmissionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/support")
public class TechnicalSupportController {

    private final SupportSubmissionService supportSubmissionService;
    private final SupportSubmissionNoteService supportSubmissionNoteService;

    public TechnicalSupportController(SupportSubmissionService supportSubmissionService, SupportSubmissionNoteService supportSubmissionNoteService) {
        this.supportSubmissionService = supportSubmissionService;
        this.supportSubmissionNoteService = supportSubmissionNoteService;
    }

    @PostMapping("/submissions")
    public ResponseEntity<MessageResponseDTO> createNewTechnicalSupportSubmission(@RequestBody SupportRequestDTO supportRequestDTO) {

        String submissionUuid = supportSubmissionService.createNewSubmission(supportRequestDTO);

        return ResponseEntity.created(URI.create(String.format("/submissions/%s", submissionUuid))).body(new MessageResponseDTO("Technical support submission created"));
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

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/submissions/{uuid}/notes")
    public ResponseEntity<MessageResponseDTO> createSubmissionNote(@PathVariable("uuid") String supportSubmissionUuid, @RequestBody SupportSubmissionCreateRequestDTO supportSubmissionCreateRequestDTO) {

        String noteUuid = supportSubmissionService.addNote(supportSubmissionUuid, supportSubmissionCreateRequestDTO);

        return ResponseEntity.created(URI.create(String.format("/submissions/%s/notes/%s", supportSubmissionUuid, noteUuid))).body(new MessageResponseDTO("Note created"));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/submissions/{uuid}/notes")
    public ResponseEntity<SupportSubmissionNotesResponseDTO> getSubmissionNotes(@PathVariable("uuid") String supportSubmissionUuid) {

        SupportSubmission supportSubmission = supportSubmissionService.getSubmissionByUuid(supportSubmissionUuid);

        return ResponseEntity.ok().body(new SupportSubmissionNotesResponseDTO(supportSubmission.getNotes()));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/submissions/notes/{noteUuid}")
    public ResponseEntity<MessageResponseDTO> deleteSubmissionNote(@PathVariable("noteUuid") String noteUuid) {
        supportSubmissionNoteService.deleteSupportSubmissionNote(noteUuid);
        return ResponseEntity.ok().body(new MessageResponseDTO("Submission note deleted"));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/submissions/notes/{uuid}")
    public ResponseEntity<MessageResponseDTO> patchSubmissionNotes(@PathVariable("uuid") String noteUuid, @RequestBody SupportSubmissionNotePatchRequestDTO supportSubmissionNotePatchRequestDTO) {
        supportSubmissionNoteService.pathSupportSubmissionNote(noteUuid, supportSubmissionNotePatchRequestDTO);
        return ResponseEntity.status(204).build();
    }
}
