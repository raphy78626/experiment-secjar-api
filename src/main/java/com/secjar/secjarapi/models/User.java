package com.secjar.secjarapi.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@Getter
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String uuid;

    private String username;
    private String password;
    private String email;
    private Boolean verified = false;
    @Setter
    private byte[] cryptographicKeyIndex;
    private long fileDeletionDelay = 2_592_000_000L;
    @ManyToMany
    @JoinTable(
            name = "users_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private List<UserRole> roles;

    @OneToMany(mappedBy = "user")
    private List<FileInfo> files = new ArrayList<>();

    public User(String uuid, String username, String password, String email, List<UserRole> roles) {
        this.uuid = uuid;
        this.username = username;
        this.password = password;
        this.email = email;
        this.roles = roles;
    }
}
