package com.secjar.secjarapi.repositories;

import com.secjar.secjarapi.models.SupportSubmissionNote;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Transactional
@Repository
public interface SupportSubmissionNoteRepository extends JpaRepository<SupportSubmissionNote, Long> {
    void deleteByUuid(String noteUuid);
}
