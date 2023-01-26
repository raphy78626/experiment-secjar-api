package com.secjar.secjarapi.services;

import com.secjar.secjarapi.models.FileInfo;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.List;

@Service
public class TaskSchedulingService {

    private final FileInfoService fileInfoService;
    private final FileService fileService;

    public TaskSchedulingService(FileInfoService fileInfoService, FileService fileService) {
        this.fileInfoService = fileInfoService;
        this.fileService = fileService;
    }

    @Scheduled(fixedDelay = 86400000)
    public void removeOutDatedOrders() {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());

        List<FileInfo> filesToDelete = fileInfoService.findAllWithDeleteDateLessThan(timestamp);

        filesToDelete.forEach(fileInfo -> {
            fileService.deleteFile(fileInfo.getUuid());
            fileInfoService.deleteFileInfoByUuid(fileInfo.getUuid());
        });
    }
}
