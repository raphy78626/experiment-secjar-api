package com.secjar.secjarapi.services;

import com.secjar.secjarapi.models.FileInfo;
import com.secjar.secjarapi.repositories.FileInfoRepository;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

    public List<FileInfo> findAllWithDeleteDateLessThan(Timestamp timestamp) {
        List<Optional<FileInfo>> filesToDelete = fileInfoRepository.findAllByDeleteDateLessThan(timestamp);

        if(filesToDelete.isEmpty()) {
            return Collections.emptyList();
        }

        return filesToDelete.stream().flatMap(Optional::stream).collect(Collectors.toList());
    }

    public void removeDeleteDate(String fileInfoUuid) {
        FileInfo fileInfo = fileInfoRepository.findByUuid(fileInfoUuid).orElseThrow(() -> new RuntimeException(String.format("FileInfo with uuid: %s does not exist", fileInfoUuid)));

        fileInfo.setDeleteDate(null);

        fileInfoRepository.save(fileInfo);
    }
}
