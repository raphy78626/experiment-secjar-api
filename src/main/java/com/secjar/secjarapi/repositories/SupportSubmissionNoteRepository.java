package com.secjar.secjarapi.repositories;

import com.secjar.secjarapi.models.SupportSubmissionNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SupportSubmissionNoteRepository extends JpaRepository<SupportSubmissionNote, Long> {
}
