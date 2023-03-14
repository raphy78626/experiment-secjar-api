package com.secjar.secjarapi.controllers;

import CryptoServerCXI.CryptoServerCXI;
import com.secjar.secjarapi.dtos.requests.DirectoryCreationDTO;
import com.secjar.secjarapi.dtos.requests.FileSystemEntriesShareRequestDTO;
import com.secjar.secjarapi.dtos.requests.FileSystemEntryPatchRequestDTO;
import com.secjar.secjarapi.dtos.requests.FileUploadRequestDTO;
import com.secjar.secjarapi.dtos.responses.FileSystemEntriesStructureResponseDTO;
import com.secjar.secjarapi.dtos.responses.FileSystemEntryInfoDTO;
import com.secjar.secjarapi.dtos.responses.MessageResponseDTO;
import com.secjar.secjarapi.dtos.responses.SharedFileSystemEntriesStructureResponseDTO;
import com.secjar.secjarapi.enums.ShareActionsEnum;
import com.secjar.secjarapi.models.FileSystemEntryInfo;
import com.secjar.secjarapi.models.User;
import com.secjar.secjarapi.services.*;
import org.apache.commons.io.FilenameUtils;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/fileSystemEntries")
public class FileSystemEntryController {

    private final FileSystemEntryInfoService fileSystemEntryInfoService;
    private final FileService fileService;
    private final UserService userService;
    private final HsmService hsmService;
    private final FileSystemEntryService fileSystemEntryService;
    private final DiskInfoService diskInfoService;

    public FileSystemEntryController(FileSystemEntryInfoService fileSystemEntryInfoService, FileService fileService, UserService userService, HsmService hsmService, FileSystemEntryService fileSystemEntryService, DiskInfoService diskInfoService) {
        this.fileSystemEntryInfoService = fileSystemEntryInfoService;
        this.fileService = fileService;
        this.userService = userService;
        this.hsmService = hsmService;
        this.fileSystemEntryService = fileSystemEntryService;
        this.diskInfoService = diskInfoService;
    }

    @GetMapping("/info")
    public ResponseEntity<FileSystemEntriesStructureResponseDTO> getFileSystemEntriesStructure(@AuthenticationPrincipal Jwt principal) {

        User user = getUserFromPrincipal(principal);

        List<FileSystemEntryInfo> fileSystemEntryInfoList = user.getFileSystemEntriesStructure();

        return ResponseEntity.ok(new FileSystemEntriesStructureResponseDTO(fileSystemEntryInfoList));
    }

    @GetMapping("/info/{uuid}")
    public ResponseEntity<?> getFileSystemEntryInfo(@PathVariable("uuid") String fileSystemEntryUuid, @AuthenticationPrincipal Jwt principal) {
        User user = getUserFromPrincipal(principal);

        FileSystemEntryInfo fileSystemEntryInfo = fileSystemEntryInfoService.getFileSystemEntryInfoByUuid(fileSystemEntryUuid);

        if (!fileSystemEntryInfo.getUser().equals(user)) {
            return ResponseEntity.status(403).body(new MessageResponseDTO("You don't have permission for that file"));
        }

        return ResponseEntity.ok(new FileSystemEntryInfoDTO(fileSystemEntryInfo));
    }

    @GetMapping("/info/shared")
    public ResponseEntity<SharedFileSystemEntriesStructureResponseDTO> getSharedFileSystemEntries(@AuthenticationPrincipal Jwt principal) {

        User user = getUserFromPrincipal(principal);

        List<FileSystemEntryInfo> fileSystemEntryInfoList = user.getSharedFileSystemEntriesStructure();

        return ResponseEntity.ok(new SharedFileSystemEntriesStructureResponseDTO(fileSystemEntryInfoList));
    }

    @GetMapping("/{uuid}")
    public ResponseEntity<?> downloadFileSystemEntry(@PathVariable(name = "uuid") String fileSystemEntryUuid, @AuthenticationPrincipal Jwt principal) {

        FileSystemEntryInfo fileSystemEntryInfo = fileSystemEntryInfoService.getFileSystemEntryInfoByUuid(fileSystemEntryUuid);

        if (!fileSystemEntryInfo.isSharedByLink()) {
            if (principal == null || !fileSystemEntryInfo.getAuthorizedUsers().contains(getUserFromPrincipal(principal))) {
                return ResponseEntity.status(403).body(new MessageResponseDTO("You don't have permission for that file"));
            }
        }

        CryptoServerCXI.Key keyForDecryption = hsmService.getKeyFromStore(fileSystemEntryInfo.getUser().getCryptographicKeyIndex());

        if (!fileSystemEntryInfo.getContentType().equals("directory")) {
            byte[] fileBytes = fileService.getFileBytes(fileSystemEntryInfo, keyForDecryption);

            ByteArrayResource byteArrayResource = new ByteArrayResource(fileBytes);

            return ResponseEntity
                    .ok()
                    .contentLength(byteArrayResource.contentLength())
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(byteArrayResource);
        }

        ByteArrayOutputStream zippedDirectory = fileSystemEntryService.getZippedDirectory(fileSystemEntryUuid, keyForDecryption);

        return ResponseEntity
                .ok()
                .contentLength(zippedDirectory.size())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(zippedDirectory.toByteArray());
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MessageResponseDTO> uploadFile(@ModelAttribute FileUploadRequestDTO fileUploadDTO, @AuthenticationPrincipal Jwt principal) {

        User user = getUserFromPrincipal(principal);

        MultipartFile multipartFile = fileUploadDTO.file();

        if (diskInfoService.getDisallowedContentTypes().contains(multipartFile.getContentType())) {
            return ResponseEntity.status(400).body(new MessageResponseDTO("File content type is not allowed"));
        }

        FileSystemEntryInfo fileSystemEntryInfo;

        if (fileUploadDTO.parentDirectoryUuid() != null) {

            FileSystemEntryInfo parent = fileSystemEntryInfoService.getFileSystemEntryInfoByUuid(fileUploadDTO.parentDirectoryUuid());
            if (!parent.getContentType().equals("directory")) {
                return ResponseEntity.status(400).body(new MessageResponseDTO("Parent is not a directory"));
            }

            fileSystemEntryInfo = new FileSystemEntryInfo(UUID.randomUUID().toString(), FilenameUtils.removeExtension(multipartFile.getOriginalFilename()), multipartFile.getContentType(), multipartFile.getSize(), parent, user);
        } else {
            fileSystemEntryInfo = new FileSystemEntryInfo(UUID.randomUUID().toString(), FilenameUtils.removeExtension(multipartFile.getOriginalFilename()), multipartFile.getContentType(), multipartFile.getSize(), user);
        }

        if (fileUploadDTO.replace()) {
            fileSystemEntryService.deleteFileSystemEntryByName(fileSystemEntryInfo.getName());
        }

        fileSystemEntryService.saveFile(user, fileSystemEntryInfo, fileUploadDTO.file());

        return ResponseEntity.created(URI.create(String.format("/file/%s", fileSystemEntryInfo.getUuid()))).body(new MessageResponseDTO("File created"));
    }

    @PostMapping
    public ResponseEntity<MessageResponseDTO> createDirectory(@RequestBody DirectoryCreationDTO directoryCreationDTO, @AuthenticationPrincipal Jwt principal) {

        User user = getUserFromPrincipal(principal);

        FileSystemEntryInfo directoryInfo;

        if (directoryCreationDTO.parentDirectoryUuid() != null) {
            FileSystemEntryInfo parent = fileSystemEntryInfoService.getFileSystemEntryInfoByUuid(directoryCreationDTO.parentDirectoryUuid());

            if (!parent.getContentType().equals("directory")) {
                return ResponseEntity.status(400).body(new MessageResponseDTO("Parent is not a directory"));
            }

            directoryInfo = new FileSystemEntryInfo(UUID.randomUUID().toString(), directoryCreationDTO.directoryName(), "directory", 0, parent, user);
        } else {
            directoryInfo = new FileSystemEntryInfo(UUID.randomUUID().toString(), directoryCreationDTO.directoryName(), "directory", 0, user);
        }

        fileSystemEntryService.saveDirectory(directoryInfo);

        return ResponseEntity.created(URI.create(String.format("/file/%s", directoryInfo.getUuid()))).body(new MessageResponseDTO("Directory created"));
    }

    @PostMapping("/{uuid}/copy")
    public ResponseEntity<MessageResponseDTO> copyFile(@PathVariable("uuid") String fileUuid, @AuthenticationPrincipal Jwt principal) {
        User user = getUserFromPrincipal(principal);

        FileSystemEntryInfo fileSystemEntryInfo = fileSystemEntryInfoService.getFileSystemEntryInfoByUuid(fileUuid);

        if (!fileSystemEntryInfo.getUser().equals(user)) {
            return ResponseEntity.status(403).body(new MessageResponseDTO("You don't have permission for that file"));
        }

        if (fileSystemEntryInfo.getContentType().equals("directory")) {
            return ResponseEntity.status(400).body(new MessageResponseDTO("Target is not a file"));
        }

        FileSystemEntryInfo copiedFileInfo = fileSystemEntryService.createFileCopy(fileSystemEntryInfo);

        return ResponseEntity.created(URI.create(String.format("/file/%s", copiedFileInfo.getUuid()))).body(new MessageResponseDTO("File created"));
    }


    @DeleteMapping("/{uuid}")
    public ResponseEntity<MessageResponseDTO> deleteFileSystemEntry(@PathVariable("uuid") String fileSystemEntryUuid, @RequestParam boolean instantDelete, @AuthenticationPrincipal Jwt principal) {

        User user = getUserFromPrincipal(principal);

        FileSystemEntryInfo fileSystemEntryInfo = fileSystemEntryInfoService.getFileSystemEntryInfoByUuid(fileSystemEntryUuid);

        if (!fileSystemEntryInfo.getUser().equals(user)) {
            return ResponseEntity.status(403).body(new MessageResponseDTO("You don't have permission for that file"));
        }

        if (instantDelete) {
            fileSystemEntryService.deleteFileSystemEntry(fileSystemEntryUuid);
            return ResponseEntity.ok(new MessageResponseDTO("File deleted"));
        } else {
            fileSystemEntryInfo.setDeleteDate(new Timestamp(System.currentTimeMillis() + user.getFileDeletionDelay()));
            fileSystemEntryInfoService.saveFileSystemEntryInfo(fileSystemEntryInfo);
            return ResponseEntity.ok(new MessageResponseDTO("File moved to trash"));
        }
    }

    @PostMapping("/{uuid}/restore")
    public ResponseEntity<MessageResponseDTO> restoreSystemEntryFromTrash(@PathVariable("uuid") String fileSystemEntryUuid, @AuthenticationPrincipal Jwt principal) {

        User user = getUserFromPrincipal(principal);

        FileSystemEntryInfo fileSystemEntryInfo = fileSystemEntryInfoService.getFileSystemEntryInfoByUuid(fileSystemEntryUuid);

        if (!fileSystemEntryInfo.getUser().equals(user)) {
            return ResponseEntity.status(403).body(new MessageResponseDTO("You don't have permission for that file"));
        }

        fileSystemEntryService.removeDeleteDate(fileSystemEntryUuid);

        return ResponseEntity.ok(new MessageResponseDTO("File restored"));
    }

    @PatchMapping("/{uuid}")
    public ResponseEntity<MessageResponseDTO> addFileSystemEntryToFavourites(@PathVariable("uuid") String fileSystemEntryUuid, @RequestBody FileSystemEntryPatchRequestDTO fileSystemEntryPatchRequestDTO, @AuthenticationPrincipal Jwt principal) {
        User user = getUserFromPrincipal(principal);

        FileSystemEntryInfo fileSystemEntryInfo = fileSystemEntryInfoService.getFileSystemEntryInfoByUuid(fileSystemEntryUuid);

        if (!fileSystemEntryInfo.getUser().equals(user)) {
            return ResponseEntity.status(403).body(new MessageResponseDTO("You don't have permission for that file"));
        }

        fileSystemEntryService.patchFileSystemEntry(fileSystemEntryUuid, fileSystemEntryPatchRequestDTO);

        return ResponseEntity.status(204).build();
    }

    @PostMapping("/share")
    public ResponseEntity<MessageResponseDTO> updateFileSystemEntryShare(@RequestBody FileSystemEntriesShareRequestDTO fileSystemEntriesShareRequestDTO, @AuthenticationPrincipal Jwt principal) {
        User user = getUserFromPrincipal(principal);

        Set<FileSystemEntryInfo> filesToShare = new HashSet<>();

        for (String fileUuid : fileSystemEntriesShareRequestDTO.fileSystemEntriesUuid()) {
            FileSystemEntryInfo fileSystemEntryInfo = fileSystemEntryInfoService.getFileSystemEntryInfoByUuid(fileUuid);

            if (!fileSystemEntryInfo.getUser().equals(user)) {
                return ResponseEntity.status(403).body(new MessageResponseDTO(String.format("You don't have permission for that file %s", fileUuid)));
            }

            filesToShare.add(fileSystemEntryInfo);
        }

        fileSystemEntryService.shareFiles(fileSystemEntriesShareRequestDTO.shareType(), fileSystemEntriesShareRequestDTO.shareAction(), filesToShare, fileSystemEntriesShareRequestDTO.usersUuids());


        if (fileSystemEntriesShareRequestDTO.shareAction() == ShareActionsEnum.SHARE_START) {
            return ResponseEntity.ok(new MessageResponseDTO("Files shared"));
        } else {
            return ResponseEntity.ok(new MessageResponseDTO("Files unshared"));
        }
    }

    private User getUserFromPrincipal(Jwt principal) {
        String userUuid = principal.getClaims().get("userUuid").toString();
        return userService.getUserByUuid(userUuid);
    }
}
