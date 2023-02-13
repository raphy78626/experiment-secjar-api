package com.secjar.secjarapi.repositories;

import com.secjar.secjarapi.models.SupportSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SupportSubmissionRepository extends JpaRepository<SupportSubmission, Long> {
}
