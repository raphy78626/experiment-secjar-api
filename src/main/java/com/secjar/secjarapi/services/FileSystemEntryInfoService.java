package com.secjar.secjarapi.services;

import com.secjar.secjarapi.models.FileSystemEntryInfo;
import com.secjar.secjarapi.models.User;
import com.secjar.secjarapi.repositories.FileSystemEntryInfoRepository;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class FileSystemEntryInfoService {

    private final FileSystemEntryInfoRepository fileSystemEntryInfoRepository;
    private final UserService userService;

    public FileSystemEntryInfoService(FileSystemEntryInfoRepository fileSystemEntryInfoRepository, UserService userService) {
        this.fileSystemEntryInfoRepository = fileSystemEntryInfoRepository;
        this.userService = userService;
    }

    public void saveFileSystemEntryInfo(FileSystemEntryInfo fileSystemEntryInfo) {
        if (fileSystemEntryInfo.getSize() + fileSystemEntryInfo.getUser().getCurrentDiskSpace() > fileSystemEntryInfo.getUser().getAllowedDiskSpace()) {
            throw new IllegalStateException(String.format("Can't save file with uuid %s. Allowed disc size exceeded", fileSystemEntryInfo.getUuid()));
        }

        userService.increaseTakenDiskSpace(fileSystemEntryInfo.getUser().getUuid(), fileSystemEntryInfo.getSize());

        fileSystemEntryInfoRepository.save(fileSystemEntryInfo);
    }

    public void deleteFileSystemEntryInfoByUuid(String fileSystemEntryInfoUuid) {
        fileSystemEntryInfoRepository.deleteByUuid(fileSystemEntryInfoUuid);
    }

    public FileSystemEntryInfo getFileSystemEntryInfoByUuid(String fileSystemEntryInfoUuid) {
        //TODO: create custom exception
        return fileSystemEntryInfoRepository.findByUuid(fileSystemEntryInfoUuid).orElseThrow(() -> new RuntimeException(String.format("FileSystemEntryInfo with uuid: %s does not exist", fileSystemEntryInfoUuid)));
    }

    public List<FileSystemEntryInfo> getAllWithDeleteDateLessThan(Timestamp timestamp) {
        List<Optional<FileSystemEntryInfo>> filesToDelete = fileSystemEntryInfoRepository.findAllByDeleteDateLessThan(timestamp);

        if (filesToDelete.isEmpty()) {
            return Collections.emptyList();
        }

        return filesToDelete.stream().flatMap(Optional::stream).collect(Collectors.toList());
    }

    public List<FileSystemEntryInfo> getAllByContentType(User user, String contentType) {
        List<Optional<FileSystemEntryInfo>> files = fileSystemEntryInfoRepository.findAllByUserAndContentType(user, contentType);

        if (files.isEmpty()) {
            return Collections.emptyList();
        }

        return files.stream().flatMap(Optional::stream).collect(Collectors.toList());
    }
}
