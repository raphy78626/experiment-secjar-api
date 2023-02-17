package com.secjar.secjarapi.models;


import com.secjar.secjarapi.enums.SupportSubmissionStatesEnum;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Entity
@Table(name = "technical_support_submissions")
public class SupportSubmission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String uuid;

    private String name;
    private String surname;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Enumerated(EnumType.STRING)
    @Setter
    private SupportSubmissionStatesEnum state;

    public SupportSubmission(String uuid, String name, String surname, String message) {
        this.uuid = uuid;
        this.name = name;
        this.surname = surname;
        this.message = message;
        this.state = SupportSubmissionStatesEnum.PENDING;
    }
}