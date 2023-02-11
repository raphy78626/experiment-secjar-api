package com.secjar.secjarapi.services;

import com.secjar.secjarapi.dtos.requests.DiskInfoPatchRequestDTO;
import com.secjar.secjarapi.models.DiskInfo;
import com.secjar.secjarapi.repositories.DiskInfoRepository;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class DiskInfoService {

    private final DiskInfoRepository diskInfoRepository;
    private final DiskInfo diskInfo;

    public DiskInfoService(DiskInfoRepository diskInfoRepository) {
        this.diskInfoRepository = diskInfoRepository;

        diskInfo = diskInfoRepository.findById(1L).orElse(new DiskInfo());
    }

    public Set<String> getDisallowedContentTypes() {
        return diskInfo.getDisallowedContentTypes();
    }

    public void patchDiskInfo(DiskInfoPatchRequestDTO diskInfoPatchRequestDTO) {
        if (diskInfoPatchRequestDTO.disallowedMimeTypes() != null) {
            diskInfo.setDisallowedContentTypes(diskInfoPatchRequestDTO.disallowedMimeTypes());
        }

        if (diskInfoPatchRequestDTO.maxUserSessionTime() != null) {
            diskInfo.setMaxUserSessionTime(diskInfoPatchRequestDTO.maxUserSessionTime());
        }

        diskInfoRepository.save(diskInfo);
    }

    public long getMaxUserSessionTime() {
        return diskInfo.getMaxUserSessionTime();
    }
}
