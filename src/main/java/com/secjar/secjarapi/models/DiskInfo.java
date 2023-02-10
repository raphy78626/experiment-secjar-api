package com.secjar.secjarapi.models;

import lombok.Getter;
import org.springframework.util.MimeType;

import java.util.HashSet;
import java.util.Set;

@Getter
public class DiskInfo {
    private final Set<MimeType> disallowedContentTypes = new HashSet<>();
}
