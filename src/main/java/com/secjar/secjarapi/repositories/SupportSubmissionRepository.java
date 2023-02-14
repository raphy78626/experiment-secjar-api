package com.secjar.secjarapi.repositories;

import com.secjar.secjarapi.enums.SupportSubmissionStatesEnum;
import com.secjar.secjarapi.models.SupportSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SupportSubmissionRepository extends JpaRepository<SupportSubmission, Long> {
    List<Optional<SupportSubmission>> findAllByState(SupportSubmissionStatesEnum supportSubmissionStatesEnum);

    Optional<SupportSubmission> findByUuid(String supportSubmissionUuid);
}
