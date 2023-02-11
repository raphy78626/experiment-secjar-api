package com.secjar.secjarapi.services;

import com.secjar.secjarapi.dtos.requests.DiskInfoPatchRequestDTO;
import com.secjar.secjarapi.models.DiskInfo;
import com.secjar.secjarapi.repositories.DiskInfoRepository;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class DiskInfoService {

    private final DiskInfoRepository diskInfoRepository;
    private final long diskId;

    public DiskInfoService(DiskInfoRepository diskInfoRepository) {
        this.diskInfoRepository = diskInfoRepository;

        DiskInfo diskInfo = new DiskInfo();
        diskInfoRepository.save(diskInfo);
        diskId = diskInfo.getId();
    }

    public Set<String> getDisallowedContentTypes() {
        DiskInfo diskInfo = diskInfoRepository.findById(diskId).orElseThrow(() -> new IllegalStateException("Disk info doesn't exist"));

        return diskInfo.getDisallowedContentTypes();
    }

    public void patchDiskInfo(DiskInfoPatchRequestDTO diskInfoPatchRequestDTO) {
        DiskInfo diskInfo = diskInfoRepository.findById(diskId).orElseThrow(() -> new IllegalStateException("Disk info doesn't exist"));

        if (diskInfoPatchRequestDTO.disallowedMimeTypes() != null) {
            diskInfo.setDisallowedContentTypes(diskInfoPatchRequestDTO.disallowedMimeTypes());
        }

        diskInfoRepository.save(diskInfo);
    }
}
