package com.example.log_service.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import com.example.log_service.entity.Log;
import com.example.log_service.service.LogService;

import java.util.Map;

@RestController
@RequestMapping("/logs")
public class LogController {

    @Autowired
    private LogService logService;

    @GetMapping
    public ResponseEntity<Page<Log>> getLogs(
            @RequestParam(required = false) String serviceName,
            @RequestParam(required = false) String logLevel,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "timestamp,desc") String sort) {
        if (page < 0 || size <= 0) {
            return ResponseEntity.badRequest().body(null);
        }
        Sort sortBy;
        try {
            String[] sortParts = sort.split(",");
            if (sortParts.length != 2) {
                throw new IllegalArgumentException("Invalid sort format. Use 'field,direction' (e.g., 'timestamp,desc')");
            }
            sortBy = Sort.by(sortParts[0].trim());
            if ("desc".equalsIgnoreCase(sortParts[1].trim())) {
                sortBy = sortBy.descending();
            } else if (!"asc".equalsIgnoreCase(sortParts[1].trim())) {
                throw new IllegalArgumentException("Sort direction must be 'asc' or 'desc'");
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }
        PageRequest pageRequest = PageRequest.of(page, size, sortBy);
        Page<Log> logs = logService.findLogs(serviceName, logLevel, keyword, pageRequest);
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Long>> getLogStats(
            @RequestParam(required = false) String serviceName,
            @RequestParam(required = false) String logLevel,
            @RequestParam(required = false) String keyword) {
        try {
            Map<String, Long> stats = logService.getLogStats(serviceName, logLevel, keyword);
            return ResponseEntity.ok(stats);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
}