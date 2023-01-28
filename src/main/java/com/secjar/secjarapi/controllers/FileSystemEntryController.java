package com.secjar.secjarapi.controllers;

import CryptoServerCXI.CryptoServerCXI;
import com.secjar.secjarapi.dtos.requests.FileUploadRequestDTO;
import com.secjar.secjarapi.dtos.responses.FileSystemEntriesStructureResponseDTO;
import com.secjar.secjarapi.dtos.responses.MessageResponseDTO;
import com.secjar.secjarapi.models.FileSystemEntryInfo;
import com.secjar.secjarapi.models.User;
import com.secjar.secjarapi.services.FileService;
import com.secjar.secjarapi.services.FileSystemEntryInfoService;
import com.secjar.secjarapi.services.HsmService;
import com.secjar.secjarapi.services.UserService;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/fileSystemEntries")
public class FileSystemEntryController {

    private final FileSystemEntryInfoService fileSystemEntryInfoService;
    private final FileService fileService;
    private final UserService userService;
    private final HsmService hsmService;

    public FileSystemEntryController(FileSystemEntryInfoService fileSystemEntryInfoService, FileService fileService, UserService userService, HsmService hsmService) {
        this.fileSystemEntryInfoService = fileSystemEntryInfoService;
        this.fileService = fileService;
        this.userService = userService;
        this.hsmService = hsmService;
    }

    @GetMapping("/info")
    public ResponseEntity<FileSystemEntriesStructureResponseDTO> getFileSystemEntriesStructure(@AuthenticationPrincipal Jwt principal) {

        User user = getUserFromPrincipal(principal);

        List<FileSystemEntryInfo> fileSystemEntryInfoList = user.getFileSystemEntriesStructure();

        return ResponseEntity.ok(new FileSystemEntriesStructureResponseDTO(fileSystemEntryInfoList));
    }

    @GetMapping("/{fileUuid}")
    public ResponseEntity<?> downloadFileSystemEntry(@PathVariable String fileUuid, @AuthenticationPrincipal Jwt principal) {

        User user = getUserFromPrincipal(principal);

        FileSystemEntryInfo fileSystemEntryInfo = fileSystemEntryInfoService.findFileIntoByUuid(fileUuid);

        if (!fileSystemEntryInfo.getUser().equals(user)) {
            return ResponseEntity.status(403).body(new MessageResponseDTO("You don't have permission for that file"));
        }

        CryptoServerCXI.Key keyForDecryption = hsmService.getKeyFromStore(user.getCryptographicKeyIndex());

        byte[] fileBytes = fileService.getFileBytes(fileSystemEntryInfo, keyForDecryption);

        ByteArrayResource byteArrayResource = new ByteArrayResource(fileBytes);

        return ResponseEntity
                .ok()
                .contentLength(byteArrayResource.contentLength())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(byteArrayResource);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MessageResponseDTO> uploadFile(@ModelAttribute FileUploadRequestDTO fileUploadDTO, @AuthenticationPrincipal Jwt principal) {

        User user = getUserFromPrincipal(principal);

        MultipartFile multipartFile = fileUploadDTO.file();

        FileSystemEntryInfo fileSystemEntryInfo = new FileSystemEntryInfo(UUID.randomUUID().toString(), multipartFile.getOriginalFilename(),multipartFile.getContentType() , user);

        fileSystemEntryInfoService.saveFileSystemEntryInfo(fileSystemEntryInfo);

        CryptoServerCXI.Key userCryptoKey = hsmService.getKeyFromStore(user.getCryptographicKeyIndex());
        fileService.saveAttachment(fileUploadDTO.file(), fileSystemEntryInfo, userCryptoKey);

        return ResponseEntity.created(URI.create(String.format("/file/%s", fileSystemEntryInfo.getUuid()))).body(new MessageResponseDTO("File created"));
    }

    @PostMapping
    public ResponseEntity<MessageResponseDTO> createDirectory(@RequestBody DirectoryCreationDTO directoryCreationDTO, @AuthenticationPrincipal Jwt principal) {

        User user = getUserFromPrincipal(principal);

        FileSystemEntryInfo fileInfo = new FileSystemEntryInfo(UUID.randomUUID().toString(), directoryCreationDTO.directoryName(), "directory", user);

        fileSystemEntryInfoService.saveFileSystemEntryInfo(fileInfo);

        return ResponseEntity.created(URI.create(String.format("/file/%s", fileInfo.getUuid()))).body(new MessageResponseDTO("Directory created"));
    }

    @DeleteMapping("/{fileUuid}")
    public ResponseEntity<MessageResponseDTO> deleteFileSystemEntry(@PathVariable String fileUuid, @RequestParam boolean instantDelete, @AuthenticationPrincipal Jwt principal) {

        User user = getUserFromPrincipal(principal);

        FileSystemEntryInfo fileSystemEntryInfo = fileSystemEntryInfoService.findFileIntoByUuid(fileUuid);

        if (!fileSystemEntryInfo.getUser().equals(user)) {
            return ResponseEntity.status(403).body(new MessageResponseDTO("You don't have permission for that file"));
        }

        if (instantDelete) {
            fileSystemEntryInfoService.deleteFileSystemEntryInfoByUuid(fileUuid);
            fileService.deleteFile(fileUuid);
            return ResponseEntity.ok(new MessageResponseDTO("File deleted"));
        } else {
            fileSystemEntryInfo.setDeleteDate(new Timestamp(System.currentTimeMillis() + user.getFileDeletionDelay()));
            fileSystemEntryInfoService.saveFileSystemEntryInfo(fileSystemEntryInfo);
            return ResponseEntity.ok(new MessageResponseDTO("File moved to trash"));
        }
    }

    @PatchMapping("/restore/{fileUuid}")
    public ResponseEntity<MessageResponseDTO> restoreSystemEntryFromTrash(@PathVariable String fileUuid, @AuthenticationPrincipal Jwt principal) {

        User user = getUserFromPrincipal(principal);

        FileSystemEntryInfo fileSystemEntryInfo = fileSystemEntryInfoService.findFileIntoByUuid(fileUuid);

        if (!fileSystemEntryInfo.getUser().equals(user)) {
            return ResponseEntity.status(403).body(new MessageResponseDTO("You don't have permission for that file"));
        }

        fileSystemEntryInfoService.removeDeleteDate(fileUuid);

        return ResponseEntity.ok(new MessageResponseDTO("File restored"));
    }

    private User getUserFromPrincipal(Jwt principal) {
        String userUuid = principal.getClaims().get("userUuid").toString();
        return userService.getUserByUuid(userUuid);
    }
}
