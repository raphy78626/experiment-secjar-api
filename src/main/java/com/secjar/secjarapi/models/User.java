package com.secjar.secjarapi.models;

import com.fasterxml.jackson.annotation.JsonValue;
import com.secjar.secjarapi.enums.MFATypeEnum;
import dev.samstevens.totp.secret.DefaultSecretGenerator;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@NoArgsConstructor
@Getter
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @JsonValue
    private String uuid;

    private String username;
    private String name;
    private String surname;
    @Setter
    private String password;
    private String email;

    @Setter
    private boolean isVerified = false;

    @Setter
    private byte[] cryptographicKeyIndex;

    @Setter
    private long fileDeletionDelay = 2_592_000_000L;

    @Setter
    @Enumerated(EnumType.STRING)
    private MFATypeEnum mfaType;
    private String mFASecret;

    @Setter
    private long allowedDiskSpace;
    @Setter
    private long currentDiskSpace;

    @ManyToMany
    @JoinTable(
            name = "users_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<UserRole> roles;

    @OneToMany(mappedBy = "user")
    private Set<FileSystemEntryInfo> fileSystemEntries = new HashSet<>();

    @ManyToMany(mappedBy = "authorizedUsers")
    private Set<FileSystemEntryInfo> sharedFileSystemEntries = new HashSet<>();

    @OneToOne(mappedBy = "user")
    private EmailOTPToken emailOTPToken;

    public User(String uuid, String username, String name, String surname, String password, String email, Set<UserRole> roles) {
        this.uuid = uuid;
        this.username = username;
        this.name = name;
        this.surname = surname;
        this.password = password;
        this.email = email;
        this.roles = roles;
        this.mfaType = MFATypeEnum.NONE;
        this.mFASecret = new DefaultSecretGenerator().generate();
    }

    public List<FileSystemEntryInfo> getFileSystemEntriesStructure() {
        return fileSystemEntries.stream().filter(fileInfo -> fileInfo.getParent() == null).toList();
    }

    public List<FileSystemEntryInfo> getSharedFileSystemEntriesStructure() {
        return sharedFileSystemEntries.stream().filter(fileInfo -> fileInfo.getUser() != this).filter(fileInfo -> fileInfo.getParent() == null).toList();
    }
}
