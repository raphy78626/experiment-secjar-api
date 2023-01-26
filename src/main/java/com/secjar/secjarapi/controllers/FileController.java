package com.secjar.secjarapi.controllers;

import CryptoServerCXI.CryptoServerCXI;
import com.secjar.secjarapi.dtos.requests.FileDeleteRequestDTO;
import com.secjar.secjarapi.dtos.requests.FileUploadRequestDTO;
import com.secjar.secjarapi.dtos.responses.MessageResponseDTO;
import com.secjar.secjarapi.models.FileInfo;
import com.secjar.secjarapi.models.User;
import com.secjar.secjarapi.services.FileInfoService;
import com.secjar.secjarapi.services.FileService;
import com.secjar.secjarapi.services.HsmService;
import com.secjar.secjarapi.services.UserService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.sql.Timestamp;
import java.util.UUID;

@RestController
@RequestMapping("/file")
public class FileController {

    private final FileInfoService fileInfoService;
    private final FileService fileService;
    private final UserService userService;
    private final HsmService hsmService;

    public FileController(FileInfoService fileInfoService, FileService fileService, UserService userService, HsmService hsmService) {
        this.fileInfoService = fileInfoService;
        this.fileService = fileService;
        this.userService = userService;
        this.hsmService = hsmService;
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MessageResponseDTO> uploadFile(@AuthenticationPrincipal Jwt principal, @ModelAttribute FileUploadRequestDTO fileUploadDTO) {

        String userUuid = principal.getClaims().get("userUuid").toString();
        User user = userService.getUserByUuid(userUuid);

        MultipartFile multipartFile = fileUploadDTO.file();

        FileInfo fileInfo = new FileInfo(UUID.randomUUID().toString(), multipartFile.getOriginalFilename(), user);

        fileInfoService.saveFileInfo(fileInfo);

        CryptoServerCXI.Key userCryptoKey = hsmService.getKeyFromStore(user.getCryptographicKeyIndex());
        fileService.saveAttachment(fileUploadDTO.file(), fileInfo, userCryptoKey);

        return ResponseEntity.created(URI.create(String.format("/file/%s", fileInfo.getUuid()))).body(new MessageResponseDTO("File created"));
    }

    @PostMapping("/delete")
    public ResponseEntity<MessageResponseDTO> delete(@RequestBody FileDeleteRequestDTO fileDeleteRequestDTO, @AuthenticationPrincipal Jwt principal) {

        String userUuid = principal.getClaims().get("userUuid").toString();
        User user = userService.getUserByUuid(userUuid);

        FileInfo fileInfo = fileInfoService.findFileIntoByUuid(fileDeleteRequestDTO.fileUuid());

        if (!fileInfo.getUser().equals(user)) {
            return ResponseEntity.status(403).body(new MessageResponseDTO("You don't have permission for that file"));
        }

        if (fileDeleteRequestDTO.instantDelete()) {
            fileInfoService.deleteFileInfoByUuid(fileDeleteRequestDTO.fileUuid());
            fileService.deleteFile(fileDeleteRequestDTO.fileUuid());
            return ResponseEntity.ok(new MessageResponseDTO("File deleted"));
        } else {
            fileInfo.setDeleteDate(new Timestamp(System.currentTimeMillis() + user.getFileDeletionDelay()));
            fileInfoService.saveFileInfo(fileInfo);
            return ResponseEntity.ok(new MessageResponseDTO("File moved to trash"));
        }
    }

}
