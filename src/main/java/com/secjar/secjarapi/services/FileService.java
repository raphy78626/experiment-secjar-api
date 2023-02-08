package com.secjar.secjarapi.services;

import CryptoServerCXI.CryptoServerCXI;
import com.secjar.secjarapi.models.FileSystemEntryInfo;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class FileService {
    @Value("${file.saveLocation}")
    private String fileSavePath;

    private final HsmService hsmService;

    public FileService(HsmService hsmService) {
        this.hsmService = hsmService;
    }

    public void saveAttachment(MultipartFile multipartFile, FileSystemEntryInfo fileSystemEntryInfo, CryptoServerCXI.Key keyForEncryption) {
        Path filePath = Path.of(fileSavePath, fileSystemEntryInfo.getUuid(), fileSystemEntryInfo.getName());
        File file = new File(filePath.toUri());

        try {
            file.getParentFile().mkdir();
            file.createNewFile();
        } catch (IOException e) {
            //TODO: Handle exception
            throw new RuntimeException("Error while creating the file", e);
        }

        byte[] encryptedFile;
        try {
            encryptedFile = hsmService.encryptData(multipartFile.getBytes(), keyForEncryption);
        } catch (IOException e) {
            //TODO: Handle exception
            throw new RuntimeException("Error while encrypting the file", e);
        }

        try {
            OutputStream targetFileOutputStream = new FileOutputStream(file);

            targetFileOutputStream.write(encryptedFile);

            targetFileOutputStream.close();
        } catch (IOException e) {
            //TODO: Handle exception
            throw new RuntimeException("Error while saving the file", e);
        }
    }

    public byte[] getFileBytes(FileSystemEntryInfo fileSystemEntryInfo, CryptoServerCXI.Key keyForDecryption) {

        Path fileDirectoryPath = Path.of(fileSavePath, fileSystemEntryInfo.getUuid(), fileSystemEntryInfo.getName());

        File encryptedFile = new File(fileDirectoryPath.toUri());
        byte[] fileBytes;
        try {
            fileBytes = Files.readAllBytes(encryptedFile.toPath());
        } catch (IOException e) {
            throw new RuntimeException("Error while getting the file", e);
        }

        return hsmService.decryptData(fileBytes, keyForDecryption);
    }

    public void createFileCopy(FileSystemEntryInfo originalFileInfo, FileSystemEntryInfo copiedFileInfo) {
        Path filePath = Path.of(fileSavePath, originalFileInfo.getUuid(), originalFileInfo.getName());
        File file = new File(filePath.toUri());

        byte[] fileBytes;
        try {
            fileBytes = Files.readAllBytes(file.toPath());
        } catch (IOException e) {
            throw new RuntimeException("Error while getting the file", e);
        }


        filePath = Path.of(fileSavePath, copiedFileInfo.getUuid(), originalFileInfo.getName());
        file = new File(filePath.toUri());

        try {
            file.getParentFile().mkdir();
            file.createNewFile();
        } catch (IOException e) {
            //TODO: Handle exception
            throw new RuntimeException("Error while creating the file", e);
        }

        try {
            OutputStream targetFileOutputStream = new FileOutputStream(file);

            targetFileOutputStream.write(fileBytes);

            targetFileOutputStream.close();
        } catch (IOException e) {
            //TODO: Handle exception
            throw new RuntimeException("Error while saving the file", e);
        }
    }

    public void deleteFile(String fileUuid) {

        Path attachmentDirectoryPath = Path.of(fileSavePath, fileUuid);

        try {
            FileUtils.deleteDirectory(attachmentDirectoryPath.toFile());
        } catch (IOException e) {
            throw new RuntimeException("Error while deleting attachment", e);
        }
    }
}
