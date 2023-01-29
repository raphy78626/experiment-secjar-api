package com.secjar.secjarapi.services;

import com.secjar.secjarapi.models.FileSystemEntryInfo;
import org.springframework.stereotype.Service;

@Service
public class FileSystemEntryService {
    private final FileSystemEntryInfoService fileSystemEntryInfoService;
    private final FileService fileService;

    public FileSystemEntryService(FileSystemEntryInfoService fileSystemEntryInfoService, FileService fileService) {
        this.fileSystemEntryInfoService = fileSystemEntryInfoService;
        this.fileService = fileService;
    }

    public void moveFileToDirectory(FileSystemEntryInfo file, FileSystemEntryInfo directory) {
        file.setParent(directory);
        directory.getChildren().add(file);

        fileSystemEntryInfoService.saveFileSystemEntryInfo(file);
        fileSystemEntryInfoService.saveFileSystemEntryInfo(directory);
    }

    public void deleteFileSystemEntry(String fileSystemEntryUuid) {
        FileSystemEntryInfo entryToDelete = fileSystemEntryInfoService.findFileSystemEntryInfoByUuid(fileSystemEntryUuid);

        if(entryToDelete.getChildren().isEmpty() && !entryToDelete.getContentType().equals("directory")) {
            fileSystemEntryInfoService.deleteFileSystemEntryInfoByUuid(fileSystemEntryUuid);
            fileService.deleteFile(fileSystemEntryUuid);
            return;
        }

        entryToDelete.getChildren().forEach(childToDelete -> deleteFileSystemEntry(childToDelete.getUuid()));

        fileSystemEntryInfoService.deleteFileSystemEntryInfoByUuid(fileSystemEntryUuid);
    }
}
