package com.secjar.secjarapi.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@Getter
@Entity
@Table(name = "file_system_entries_info")
public class FileSystemEntryInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String uuid;

    private String name;
    private String contentType;

    @Setter
    private Timestamp deleteDate;

    @Setter
    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "parent_id")
    private FileSystemEntryInfo parent;

    @Setter
    @OneToMany(mappedBy = "parent", fetch = FetchType.EAGER)
    private List<FileSystemEntryInfo> children = new ArrayList<>();

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public FileSystemEntryInfo(String uuid, String name, String contentType, User user) {
        this.uuid = uuid;
        this.name = name;
        this.contentType = contentType;
        this.user = user;
    }
}
