package com.owedly.controller;

import com.owedly.dto.response.ActivityResponse;
import com.owedly.service.ActivityService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/activities")
public class ActivityController {

    private final ActivityService activityService;

    public ActivityController(ActivityService activityService) {
        this.activityService = activityService;
    }

    // =========================================================
    // MY ACTIVITIES
    // =========================================================

    @GetMapping
    public ResponseEntity<List<ActivityResponse>> getMyActivities(
            Authentication authentication,
            @RequestParam(defaultValue = "20") int limit
    ) {

        List<ActivityResponse> activities =
                activityService.getMyActivities(
                        authentication.getName(),
                        limit
                );

        return ResponseEntity.ok(activities);
    }

    // =========================================================
    // GROUP ACTIVITIES
    // =========================================================

    @GetMapping("/groups/{groupId}")
    public ResponseEntity<List<ActivityResponse>> getGroupActivities(
            @PathVariable Long groupId,
            Authentication authentication,
            @RequestParam(defaultValue = "20") int limit
    ) {

        List<ActivityResponse> activities =
                activityService.getGroupActivities(
                        groupId,
                        authentication.getName(),
                        limit
                );

        return ResponseEntity.ok(activities);
    }
}