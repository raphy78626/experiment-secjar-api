package com.secjar.secjarapi.services;

import com.secjar.secjarapi.dtos.requests.FileSystemEntryPatchRequestDTO;
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

    public void moveFileToDirectory(FileSystemEntryInfo fileSystemEntry, FileSystemEntryInfo targetFileSystemEntry) {

        if (!targetFileSystemEntry.getContentType().equals("directory")) {
            throw new IllegalArgumentException("Target is not a directory");
        }

        fileSystemEntry.setParent(targetFileSystemEntry);

        fileSystemEntryInfoService.saveFileSystemEntryInfo(fileSystemEntry);
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

    public void removeDeleteDate(String fileSystemEntryInfoUuid) {
        FileSystemEntryInfo fileSystemEntryInfo = fileSystemEntryInfoService.findFileSystemEntryInfoByUuid(fileSystemEntryInfoUuid);

        fileSystemEntryInfo.setDeleteDate(null);

        fileSystemEntryInfoService.saveFileSystemEntryInfo(fileSystemEntryInfo);
    }

    public void patchFileSystemEntry(String fileSystemEntryUuid, FileSystemEntryPatchRequestDTO fileSystemEntryPatchRequestDTO) {
        FileSystemEntryInfo fileSystemEntryInfo = fileSystemEntryInfoService.findFileSystemEntryInfoByUuid(fileSystemEntryUuid);

        if(fileSystemEntryPatchRequestDTO.isFavourite() != null) {
            fileSystemEntryInfo.setFavourite(fileSystemEntryPatchRequestDTO.isFavourite());
        }

        if(fileSystemEntryPatchRequestDTO.parentDirectoryUuid() != null) {
            FileSystemEntryInfo targetFileSystemEntry = fileSystemEntryInfoService.findFileSystemEntryInfoByUuid(fileSystemEntryPatchRequestDTO.parentDirectoryUuid());
            moveFileToDirectory(fileSystemEntryInfo, targetFileSystemEntry);
        }

        fileSystemEntryInfoService.saveFileSystemEntryInfo(fileSystemEntryInfo);
    }
}
