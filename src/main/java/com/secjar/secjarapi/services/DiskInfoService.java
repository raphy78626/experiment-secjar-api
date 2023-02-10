package com.secjar.secjarapi.services;

import com.secjar.secjarapi.models.DiskInfo;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;

import java.util.Set;

@Service
public class DiskInfoService {

    private final DiskInfo diskInfo = new DiskInfo();

    public Set<MimeType> getDisallowedContentTypes() {
        diskInfo.getDisallowedContentTypes().add(MimeTypeUtils.parseMimeType("image/jpeg"));
        return diskInfo.getDisallowedContentTypes();
    }
}
