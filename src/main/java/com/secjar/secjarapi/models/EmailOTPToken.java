package com.secjar.secjarapi.models;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor
@Getter
@Entity
@Table(name = "email_otp_tokens")
public class EmailOTPToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String token;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    public EmailOTPToken(String token, LocalDateTime createdAt, LocalDateTime expiresAt, User user) {
        this.token = token;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.user = user;
    }
}
