package com.secjar.secjarapi.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;


@NoArgsConstructor
@Getter
@Entity
@Table(name = "account_creation_credentials")
public class AccountCreationCredentials {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String username;
    private String email;

    private String token;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;

    @Setter
    private LocalDateTime usedAt;

    public AccountCreationCredentials(String username, String email, String token, LocalDateTime createdAt, LocalDateTime expiresAt) {
        this.username = username;
        this.email = email;
        this.token = token;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
    }
}
