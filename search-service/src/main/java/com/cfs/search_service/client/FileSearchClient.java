package com.cfs.search_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.Map;

@FeignClient(name = "File-service",url = "http://localhost:8081")
public interface FileSearchClient {

    @GetMapping("/api/files")
    List<Map<String,Object>> getAllFiles();

    @GetMapping("/api/files/folders/{folderId}")
    List<Map<String,Object>> getAllFolders(@PathVariable Long folderId);
}
