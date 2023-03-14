package com.secjar.secjarapi.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@NoArgsConstructor
@Getter
@Entity
@Table(name = "file_system_entries_info")
public class FileSystemEntryInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String uuid;

    @Setter
    private String name;
    private String contentType;
    private long size;

    @Setter
    private boolean isFavourite;

    @Setter
    private Timestamp deleteDate;

    @Setter
    private boolean isSharedByLink;

    private LocalDateTime uploadDate;

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

    @JsonIgnore
    @ManyToMany
    @JoinTable(
            name = "shared_files",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "file_id")
    )
    private Set<User> authorizedUsers = new HashSet<>();

    public FileSystemEntryInfo(String uuid, String name, String contentType, long size, User user) {
        this.uuid = uuid;
        this.name = name;
        this.contentType = contentType;
        this.size = size;
        this.user = user;
        this.authorizedUsers.add(user);
        uploadDate = LocalDateTime.now();
    }

    public FileSystemEntryInfo(String uuid, String name, String contentType, long size, FileSystemEntryInfo parent, User user) {
        this.uuid = uuid;
        this.name = name;
        this.contentType = contentType;
        this.size = size;
        this.parent = parent;
        this.user = user;
        this.authorizedUsers.add(user);
        uploadDate = LocalDateTime.now();
    }
}
