package com.owedly.controller;

import com.owedly.dto.response.AnalyticsResponse;
import com.owedly.service.AnalyticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping
    public ResponseEntity<AnalyticsResponse> getAnalytics(
            Authentication authentication) {

        AnalyticsResponse response =
                analyticsService.getAnalytics(authentication.getName());

        return ResponseEntity.ok(response);
    }
}