package com.secjar.secjarapi.repositories;

import com.secjar.secjarapi.models.FileSystemEntryInfo;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Repository
@Transactional
public interface FileSystemEntryInfoRepository extends JpaRepository<FileSystemEntryInfo, Long> {
    Optional<FileSystemEntryInfo> findByUuid(String fileSystemEntryInfoUuid);

    void deleteByUuid(String fileSystemEntryInfoUuid);

    List<Optional<FileSystemEntryInfo>> findAllByDeleteDateLessThan(Timestamp timestamp);
}
