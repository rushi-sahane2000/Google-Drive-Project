package com.cfs.search_service.client;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class FileServiceFallback implements FileSearchClient{
    @Override
    public List<Map<String, Object>> getAllFiles() {
        System.err.println("[FALLBACK] file service unavailable-returning empty list");
        return new ArrayList<>();
    }

    @Override
    public List<Map<String, Object>> getAllFolders(Long folderId) {
        System.err.println("[FALLBACK] file service unavailable for folder "+folderId+" - returning empty list");
        return new ArrayList<>();
    }
}
