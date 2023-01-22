package com.secjar.secjarapi.repositories;

import com.secjar.secjarapi.models.User;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    Optional<User> findByUuid(String username);

    @Transactional
    @Modifying
    @Query("UPDATE User " +
            "SET verified = TRUE WHERE email = ?1")
    int enableAppUser(String email);

}
