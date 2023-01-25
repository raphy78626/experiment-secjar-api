package com.secjar.secjarapi.repositories;

import com.secjar.secjarapi.models.FileInfo;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@Transactional
public interface FileInfoRepository extends JpaRepository<FileInfo, Long> {
    Optional<FileInfo> findByUuid(String fileInfoUuid);
    void deleteByUuid(String fileInfoUuid);
}
