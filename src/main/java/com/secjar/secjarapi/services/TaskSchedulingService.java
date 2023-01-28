package com.secjar.secjarapi.services;

import com.secjar.secjarapi.models.FileSystemEntryInfo;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.List;

@Service
public class TaskSchedulingService {

    private final FileSystemEntryInfoService fileSystemEntryInfoService;
    private final FileService fileService;

    public TaskSchedulingService(FileSystemEntryInfoService fileSystemEntryInfoService, FileService fileService) {
        this.fileSystemEntryInfoService = fileSystemEntryInfoService;
        this.fileService = fileService;
    }

    @Scheduled(fixedDelay = 86400000)
    public void removeFileSystemEntriesFromTrash() {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());

        List<FileSystemEntryInfo> filesToDelete = fileSystemEntryInfoService.findAllWithDeleteDateLessThan(timestamp);

        filesToDelete.forEach(file -> {
            fileService.deleteFile(file.getUuid());
            fileSystemEntryInfoService.deleteFileSystemEntryInfoByUuid(file.getUuid());
        });
    }
}
