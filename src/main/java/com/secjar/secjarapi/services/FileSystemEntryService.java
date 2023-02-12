package com.secjar.secjarapi.services;

import CryptoServerCXI.CryptoServerCXI;
import com.secjar.secjarapi.dtos.requests.FileSystemEntryPatchRequestDTO;
import com.secjar.secjarapi.enums.ShareActionsEnum;
import com.secjar.secjarapi.models.FileSystemEntryInfo;
import com.secjar.secjarapi.models.User;
import jodd.net.MimeTypes;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


@Service
public class FileSystemEntryService {
    private final FileSystemEntryInfoService fileSystemEntryInfoService;
    private final FileService fileService;
    private final HsmService hsmService;
    private final UserService userService;

    public FileSystemEntryService(FileSystemEntryInfoService fileSystemEntryInfoService, FileService fileService, HsmService hsmService, UserService userService) {
        this.fileSystemEntryInfoService = fileSystemEntryInfoService;
        this.fileService = fileService;
        this.hsmService = hsmService;
        this.userService = userService;
    }

    public void saveFile(User user, FileSystemEntryInfo fileInfo, MultipartFile file) {
        String fileName = getNotTakenFileName(fileInfo.getName(), fileInfo.getContentType(), fileInfo.getParent(), fileInfo.getUser());

        fileInfo.setName(fileName);

        fileSystemEntryInfoService.saveFileSystemEntryInfo(fileInfo);

        CryptoServerCXI.Key userCryptoKey = hsmService.getKeyFromStore(user.getCryptographicKeyIndex());
        fileService.saveAttachment(file, fileInfo, userCryptoKey);
    }

    public void saveDirectory(FileSystemEntryInfo directoryInfo) {
        String directoryName = getNotTakenFileName(directoryInfo.getName(), directoryInfo.getContentType(), directoryInfo.getParent(), directoryInfo.getUser());

        directoryInfo.setName(directoryName);

        fileSystemEntryInfoService.saveFileSystemEntryInfo(directoryInfo);
    }

    public void moveFileToDirectory(FileSystemEntryInfo fileSystemEntry, FileSystemEntryInfo targetFileSystemEntry) {

        if (!targetFileSystemEntry.getContentType().equals("directory")) {
            throw new IllegalArgumentException("Target is not a directory");
        }

        fileSystemEntry.setParent(targetFileSystemEntry);

        fileSystemEntry.getAuthorizedUsers().clear();
        for (User authorizeUser : targetFileSystemEntry.getAuthorizedUsers()) {
            updateShareFileSystemEntryWithUser(fileSystemEntry, authorizeUser.getUuid(), ShareActionsEnum.START_SHARE);
        }

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

    public void deleteAllUserFileSystemEntries(User user) {
        for (FileSystemEntryInfo fileSystemEntryInfo : user.getFileSystemEntries()) {
            deleteFileSystemEntry(fileSystemEntryInfo.getUuid());
        }
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
            if (fileSystemEntryPatchRequestDTO.name() == null) {
                if (fileSystemEntryInfoService.doesFileSystemEntryInfoExists(fileSystemEntryPatchRequestDTO.parentDirectoryUuid())) {
                    FileSystemEntryInfo parentDirectory = fileSystemEntryInfoService.getFileSystemEntryInfoByUuid(fileSystemEntryPatchRequestDTO.parentDirectoryUuid());
                    fileSystemEntryInfo.setName(getNotTakenFileName(fileSystemEntryInfo.getName(), fileSystemEntryInfo.getContentType(), parentDirectory, fileSystemEntryInfo.getUser()));
                } else {
                    fileSystemEntryInfo.setName(getNotTakenFileName(fileSystemEntryInfo.getName(), fileSystemEntryInfo.getContentType(), null, fileSystemEntryInfo.getUser()));
                }
            }

            if (!fileSystemEntryPatchRequestDTO.parentDirectoryUuid().equals("")) {
                FileSystemEntryInfo targetFileSystemEntry = fileSystemEntryInfoService.getFileSystemEntryInfoByUuid(fileSystemEntryPatchRequestDTO.parentDirectoryUuid());
                moveFileToDirectory(fileSystemEntryInfo, targetFileSystemEntry);
            } else {
                fileSystemEntryInfo.setParent(null);
            }
        }

        if (fileSystemEntryPatchRequestDTO.name() != null && !fileSystemEntryPatchRequestDTO.name().isBlank()) {
            fileSystemEntryInfo.setName(getNotTakenFileName(fileSystemEntryPatchRequestDTO.name(), fileSystemEntryInfo.getContentType(), fileSystemEntryInfo.getParent(), fileSystemEntryInfo.getUser()));
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
            if (!fileSystemEntryInfo.getContentType().equals("directory")) {
                byte[] fileBytes = fileService.getFileBytes(fileSystemEntryInfo, keyForDecryption);

                try {
                    String fileExtension = MimeTypes.findExtensionsByMimeTypes(fileSystemEntryInfo.getContentType(), false)[0];
                    zipOutputStream.putNextEntry(new ZipEntry(path + fileSystemEntryInfo.getName() + "." + fileExtension));

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

    public FileSystemEntryInfo createFileCopy(FileSystemEntryInfo fileInfo) {

        String copiedFileName = getNotTakenFileName(fileInfo.getName(), fileInfo.getContentType(), fileInfo.getParent(), fileInfo.getUser());

        FileSystemEntryInfo copiedFileInfo = new FileSystemEntryInfo(UUID.randomUUID().toString(), copiedFileName, fileInfo.getContentType(), fileInfo.getSize(), fileInfo.getUser());

        fileService.createFileCopy(fileInfo, copiedFileInfo);

        fileSystemEntryInfoService.saveFileSystemEntryInfo(copiedFileInfo);

        return fileInfo;
    }

    public void updateShareFileSystemEntryWithUser(FileSystemEntryInfo fileSystemEntryInfo, String userUuid, ShareActionsEnum shareAction) {
        User user = userService.getUserByUuid(userUuid);

        if (shareAction == ShareActionsEnum.START_SHARE) {
            fileSystemEntryInfo.getAuthorizedUsers().add(user);
        } else if (shareAction == ShareActionsEnum.STOP_SHARE) {
            fileSystemEntryInfo.getAuthorizedUsers().remove(user);
        }

        fileSystemEntryInfoService.saveFileSystemEntryInfo(fileSystemEntryInfo);

        if (fileSystemEntryInfo.getContentType().equals("directory")) {
            for (FileSystemEntryInfo childFileSystemEntry : fileSystemEntryInfo.getChildren()) {
                updateShareFileSystemEntryWithUser(childFileSystemEntry, userUuid, shareAction);
            }
        }
    }

    private String getNotTakenFileName(String fileName, String contentType, FileSystemEntryInfo parentDirectory, User user) {

        List<FileSystemEntryInfo> filesWithTheSameContentType = fileSystemEntryInfoService.getAllByContentType(user, contentType).stream().filter(fileSystemEntryInfo -> fileSystemEntryInfo.getParent() == parentDirectory).toList();
        Set<String> takenNames = filesWithTheSameContentType.stream().map(FileSystemEntryInfo::getName).collect(Collectors.toSet());

        String newName = fileName;

        int i = 1;
        while (takenNames.contains(newName)) {
            newName = fileName.concat("-" + i);
            i++;
        }

        return newName;
    }
}
