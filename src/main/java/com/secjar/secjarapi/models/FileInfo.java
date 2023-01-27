package com.secjar.secjarapi.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;

@NoArgsConstructor
@Getter
@Entity
@Table(name = "files_info")
public class FileInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String uuid;

    private String fileName;
    @Setter
    private Timestamp deleteDate;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public FileInfo(String uuid, String fileName, User user) {
        this.uuid = uuid;
        this.fileName = fileName;
        this.user = user;
    }
}
