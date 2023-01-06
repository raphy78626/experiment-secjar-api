package com.secjar.secjarapi.repositories;

import com.secjar.secjarapi.enums.UserRolesEnum;
import com.secjar.secjarapi.models.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<UserRole, Long> {
    Optional<UserRole> findByRole(UserRolesEnum userRolesEnum);
}
