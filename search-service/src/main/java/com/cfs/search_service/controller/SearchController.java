package com.cfs.search_service.controller;

import com.cfs.search_service.client.FileSearchClient;
import com.cfs.search_service.client.FolderSearchClient;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

@RestController
@RequestMapping("/api/search")
public class SearchController {

    @Autowired
    private FileSearchClient fileSearchClient;

    @Autowired
    private FolderSearchClient folderSearchClient;

    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;

    @GetMapping
    public Map<String, Object> search(@RequestParam String query) {
        System.out.println("Request for query: " + query);

        CircuitBreaker fileServiceCB = circuitBreakerRegistry.circuitBreaker("File-service");
        CircuitBreaker folderServiceCB = circuitBreakerRegistry.circuitBreaker("folder-service");

        // ✅ FIX 1: Separate try-catch per service so one failure doesn't
        //           skip the other service entirely
        List<Map<String, Object>> allFiles = new ArrayList<>();
        try {
            Supplier<List<Map<String, Object>>> fileSupplier = fileSearchClient::getAllFiles;
            allFiles = fileServiceCB.executeSupplier(fileSupplier);
        } catch (CallNotPermittedException e) {
            System.out.println("[CB OPEN] File-service circuit breaker open - returning empty list");
        } catch (Exception e) {
            System.err.println("[ERROR] File-service: " + e.getMessage());
        }

        List<Map<String, Object>> allFolders = new ArrayList<>();
        try {
            Supplier<List<Map<String, Object>>> folderSupplier = folderSearchClient::getAllFolders;
            allFolders = folderServiceCB.executeSupplier(folderSupplier);
        } catch (CallNotPermittedException e) {
            System.out.println("[CB OPEN] Folder-service circuit breaker open - returning empty list");
        } catch (Exception e) {
            System.err.println("[ERROR] Folder-service: " + e.getMessage());
        }

        // ✅ FIX 2: Added null safety to folder filter (was missing, present in file filter)
        List<Map<String, Object>> fileResult = allFiles.stream()
                .filter(f -> f != null && f.get("name") != null &&
                        f.get("name").toString().toLowerCase().contains(query.toLowerCase()))
                .toList();

        List<Map<String, Object>> folderResult = allFolders.stream()
                .filter(f -> f != null && f.get("name") != null &&
                        f.get("name").toString().toLowerCase().contains(query.toLowerCase()))
                .toList();

        // ✅ FIX 3: Added folders to the response (was missing entirely)
        Map<String, Object> result = new HashMap<>();
        result.put("files", fileResult);
        result.put("folders", folderResult);
        return result;
    }

    // ✅ FIX 4: Added null safety filter (NPE risk if name field is missing)
    @GetMapping("/files")
    public List<Map<String, Object>> searchFile(@RequestParam String query) {
        List<Map<String, Object>> allFiles = fileSearchClient.getAllFiles();
        return allFiles.stream()
                .filter(f -> f != null && f.get("name") != null &&
                        f.get("name").toString().toLowerCase().contains(query.toLowerCase()))
                .toList();
    }

    // ✅ FIX 5: Added null safety filter (same NPE risk)
    @GetMapping("/folders")
    public List<Map<String, Object>> searchFolders(@RequestParam String query) {
        List<Map<String, Object>> allFolders = folderSearchClient.getAllFolders();
        return allFolders.stream()
                .filter(f -> f != null && f.get("name") != null &&
                        f.get("name").toString().toLowerCase().contains(query.toLowerCase()))
                .toList();
    }
}