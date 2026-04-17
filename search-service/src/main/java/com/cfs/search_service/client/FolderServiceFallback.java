package com.cfs.search_service.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FolderServiceFallback implements FolderSearchClient{
    @Override
    public List<Map<String, Object>> getAllFolders() {
        System.err.println("[FALLBACK] Folder service unavailable-returning empty list");
        return new ArrayList<>();
    }

    @Override
    public List<Map<String, Object>> getAllFoldersByParent(Long parentId) {
        System.err.println("[FALLBACK] folders service unavailable for folder "+parentId+" - returning empty list");
        return new ArrayList<>();
    }
}
