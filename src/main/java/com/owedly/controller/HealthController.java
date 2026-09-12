package com.owedly.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/health")
public class HealthController {

    @GetMapping
    public ResponseEntity<Map<String, Object>> health() {

        Map<String, Object> response = Map.of(
                "status", "UP",
                "service", "owedly-backend",
                "timestamp", LocalDateTime.now()
        );

        return ResponseEntity.ok(response);
    }
}