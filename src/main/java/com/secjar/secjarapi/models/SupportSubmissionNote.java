package com.secjar.secjarapi.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Entity
@Table(name = "technical_support_submission_notes")
public class SupportSubmissionNote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String uuid;

    @Column(columnDefinition = "TEXT")
    @Setter
    private String noteContent;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "support_submission_id")
    private SupportSubmission supportSubmission;

    public SupportSubmissionNote(String uuid, String noteContent, SupportSubmission supportSubmission) {
        this.uuid = uuid;
        this.noteContent = noteContent;
        this.supportSubmission = supportSubmission;
    }
}
