package com.secjar.secjarapi.controllers;

import com.secjar.secjarapi.dtos.requests.FileUploadRequestDTO;
import com.secjar.secjarapi.dtos.responses.MessageResponseDTO;
import com.secjar.secjarapi.models.FileInfo;
import com.secjar.secjarapi.models.User;
import com.secjar.secjarapi.services.FileInfoService;
import com.secjar.secjarapi.services.FileService;
import com.secjar.secjarapi.services.UserService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/file")
public class FileController {

    private final FileInfoService fileInfoService;
    private final FileService fileService;
    private final UserService userService;

    public FileController(FileInfoService fileInfoService, FileService fileService, UserService userService) {
        this.fileInfoService = fileInfoService;
        this.fileService = fileService;
        this.userService = userService;
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MessageResponseDTO> uploadFile(@AuthenticationPrincipal Jwt principal, @ModelAttribute FileUploadRequestDTO fileUploadDTO) {

        String userUuid = principal.getClaims().get("userUuid").toString();
        User user = userService.getUserByUuid(userUuid);

        MultipartFile multipartFile = fileUploadDTO.file();

        FileInfo fileInfo = new FileInfo(UUID.randomUUID().toString(), multipartFile.getOriginalFilename(), user);

        fileInfoService.saveFileInfo(fileInfo);
        fileService.saveAttachment(fileUploadDTO.file(), fileInfo);

        return ResponseEntity.created(URI.create(String.format("/file/%s", fileInfo.getUuid()))).body(new MessageResponseDTO("File created"));
    }
}
