package com.secjar.secjarapi.repositories;

import com.secjar.secjarapi.models.EmailOTPToken;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Transactional
@Repository
public interface EmailOTPTokenRepository extends JpaRepository<EmailOTPToken, Long> {
    Optional<EmailOTPToken> findByToken(String token);

    void deleteByToken(String token);
}
