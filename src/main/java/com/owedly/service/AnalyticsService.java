package com.owedly.service;

import com.owedly.dto.response.AnalyticsResponse;

public interface AnalyticsService {

    AnalyticsResponse getAnalytics(String userEmail);
}