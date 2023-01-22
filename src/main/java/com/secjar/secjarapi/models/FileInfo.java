package com.secjar.secjarapi.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

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

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public FileInfo(String uuid, String fileName, User user) {
        this.uuid = uuid;
        this.fileName = fileName;
        this.user = user;
    }
}
