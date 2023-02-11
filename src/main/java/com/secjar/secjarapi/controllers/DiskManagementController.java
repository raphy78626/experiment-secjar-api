package com.secjar.secjarapi.controllers;

import com.secjar.secjarapi.dtos.responses.DiskInfoResponseDTO;
import com.secjar.secjarapi.services.DiskInfoService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/diskInfo")
@PreAuthorize("hasRole('ADMIN')")
public class DiskManagementController {

    private final DiskInfoService diskInfoService;

    public DiskManagementController(DiskInfoService diskInfoService) {
        this.diskInfoService = diskInfoService;
    }


    @GetMapping("")
    public ResponseEntity<DiskInfoResponseDTO> getDiskInfo() {
        DiskInfoResponseDTO diskInfoResponseDTO = new DiskInfoResponseDTO(diskInfoService.getDisallowedContentTypes());

        return ResponseEntity.ok(diskInfoResponseDTO);
    }
}
