package com.secjar.secjarapi.services;

import CryptoServerCXI.CryptoServerCXI;
import com.secjar.secjarapi.dtos.requests.FileSystemEntryPatchRequestDTO;
import com.secjar.secjarapi.models.FileSystemEntryInfo;
import org.springframework.stereotype.Service;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


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
        FileSystemEntryInfo entryToDelete = fileSystemEntryInfoService.getFileSystemEntryInfoByUuid(fileSystemEntryUuid);

        if (entryToDelete.getChildren().isEmpty() && !entryToDelete.getContentType().equals("directory")) {
            fileSystemEntryInfoService.deleteFileSystemEntryInfoByUuid(fileSystemEntryUuid);
            fileService.deleteFile(fileSystemEntryUuid);
            return;
        }

        entryToDelete.getChildren().forEach(childToDelete -> deleteFileSystemEntry(childToDelete.getUuid()));

        fileSystemEntryInfoService.deleteFileSystemEntryInfoByUuid(fileSystemEntryUuid);
    }

    public void removeDeleteDate(String fileSystemEntryInfoUuid) {
        FileSystemEntryInfo fileSystemEntryInfo = fileSystemEntryInfoService.getFileSystemEntryInfoByUuid(fileSystemEntryInfoUuid);

        fileSystemEntryInfo.setDeleteDate(null);

        fileSystemEntryInfoService.saveFileSystemEntryInfo(fileSystemEntryInfo);
    }

    public void patchFileSystemEntry(String fileSystemEntryUuid, FileSystemEntryPatchRequestDTO fileSystemEntryPatchRequestDTO) {
        FileSystemEntryInfo fileSystemEntryInfo = fileSystemEntryInfoService.getFileSystemEntryInfoByUuid(fileSystemEntryUuid);

        if (fileSystemEntryPatchRequestDTO.isFavourite() != null) {
            fileSystemEntryInfo.setFavourite(fileSystemEntryPatchRequestDTO.isFavourite());
        }

        if (fileSystemEntryPatchRequestDTO.parentDirectoryUuid() != null) {
            if (!fileSystemEntryPatchRequestDTO.parentDirectoryUuid().equals("")) {
                FileSystemEntryInfo targetFileSystemEntry = fileSystemEntryInfoService.getFileSystemEntryInfoByUuid(fileSystemEntryPatchRequestDTO.parentDirectoryUuid());
                moveFileToDirectory(fileSystemEntryInfo, targetFileSystemEntry);
            } else {
                fileSystemEntryInfo.setParent(null);
            }
        }

        fileSystemEntryInfoService.saveFileSystemEntryInfo(fileSystemEntryInfo);
    }

    public ByteArrayOutputStream getZippedDirectory(String directoryUuid, CryptoServerCXI.Key keyForDecryption) {
        FileSystemEntryInfo targetDirectory = fileSystemEntryInfoService.getFileSystemEntryInfoByUuid(directoryUuid);

        if (!targetDirectory.getContentType().equals("directory")) {
            throw new IllegalArgumentException("Target is not a directory");
        }

        if (targetDirectory.getChildren().isEmpty()) {
            throw new IllegalArgumentException("Target directory is empty");
        }

        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(byteArrayOutputStream);
            ZipOutputStream zipOutputStream = new ZipOutputStream(bufferedOutputStream);

            ZippingDFSHelper(targetDirectory.getChildren(), zipOutputStream, keyForDecryption, "");

            zipOutputStream.close();
            bufferedOutputStream.close();
            byteArrayOutputStream.close();

            return byteArrayOutputStream;
        } catch (IOException e) {
            throw new RuntimeException("Error while creating zip from directory", e);
        }
    }

    private void ZippingDFSHelper(List<FileSystemEntryInfo> fileSystemEntryInfoChildren, ZipOutputStream zipOutputStream, CryptoServerCXI.Key keyForDecryption, String path) {
        for (FileSystemEntryInfo fileSystemEntryInfo : fileSystemEntryInfoChildren) {
            if (fileSystemEntryInfo.getChildren().isEmpty()) {
                byte[] fileBytes = fileService.getFileBytes(fileSystemEntryInfo, keyForDecryption);

                try {
                    zipOutputStream.putNextEntry(new ZipEntry(path + fileSystemEntryInfo.getName()));

                    zipOutputStream.write(fileBytes, 0, fileBytes.length);

                    zipOutputStream.closeEntry();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else {
                String newPath = path + fileSystemEntryInfo.getName() + "/";
                ZippingDFSHelper(fileSystemEntryInfo.getChildren(), zipOutputStream, keyForDecryption, newPath);
            }
        }
    }
}
