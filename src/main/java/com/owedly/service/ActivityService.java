package com.owedly.service;

import com.owedly.dto.response.ActivityResponse;

import java.util.List;

public interface ActivityService {

    List<ActivityResponse> getMyActivities(
            String userEmail,
            int limit
    );

    List<ActivityResponse> getGroupActivities(
            Long groupId,
            String userEmail,
            int limit
    );
}