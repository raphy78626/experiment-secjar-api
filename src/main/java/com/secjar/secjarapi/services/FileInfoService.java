package com.secjar.secjarapi.services;

import com.secjar.secjarapi.models.FileInfo;
import com.secjar.secjarapi.repositories.FileInfoRepository;
import org.springframework.stereotype.Service;

@Service
public class FileInfoService {

    private final FileInfoRepository fileInfoRepository;

    public FileInfoService(FileInfoRepository fileInfoRepository) {
        this.fileInfoRepository = fileInfoRepository;
    }

    public void saveFileInfo(FileInfo file) {
        fileInfoRepository.save(file);
    }
}
