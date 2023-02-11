package com.secjar.secjarapi.repositories;

import com.secjar.secjarapi.models.DiskInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DiskInfoRepository extends JpaRepository<DiskInfo, Long> {
}
