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

    public void deleteFileInfoByUuid(String fileInfoUuid){
        fileInfoRepository.deleteByUuid(fileInfoUuid);
    }

    public FileInfo findFileIntoByUuid(String fileInfoUuid) {
        //TODO: create custom exception
        return fileInfoRepository.findByUuid(fileInfoUuid).orElseThrow(() -> new RuntimeException(String.format("File with uuid: %s does not exist", fileInfoUuid)));
    }
}
