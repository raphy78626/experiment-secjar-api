package com.secjar.secjarapi.services;

import com.secjar.secjarapi.models.FileInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;

@Service
public class FileService {
    @Value("${file.saveLocation}")
    private String fileSavePath;

    public void saveAttachment(MultipartFile multipartFile, FileInfo fileInfo) {
        Path filePath = Path.of(fileSavePath, fileInfo.getUuid(), multipartFile.getOriginalFilename());
        File file = new File(filePath.toUri());

        try {
            file.getParentFile().mkdir();
            file.createNewFile();
        } catch (IOException e) {
            //TODO: Handle exception
            throw new RuntimeException("Error while creating the file", e);
        }

        byte[] multipartFileBytes;
        try {
            multipartFileBytes = multipartFile.getBytes();
            //TODO: Encrypt file using HSM
        } catch (IOException e) {
            //TODO: Handle exception
            throw new RuntimeException("Error while encrypting the file", e);
        }

        try {
            OutputStream targetFileOutputStream = new FileOutputStream(file);

            targetFileOutputStream.write(multipartFileBytes);

            targetFileOutputStream.close();
        } catch (IOException e) {
            //TODO: Handle exception
            throw new RuntimeException("Error while saving the file", e);
        }
    }
}
