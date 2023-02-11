package com.secjar.secjarapi.repositories;

import com.secjar.secjarapi.models.AccountCreationCredentials;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountCreationCredentialsRepository extends JpaRepository<AccountCreationCredentials, Long> {
    Optional<AccountCreationCredentials> findByToken(String token);
}
