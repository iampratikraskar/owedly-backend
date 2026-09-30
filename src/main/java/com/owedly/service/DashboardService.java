package com.owedly.service;

import com.owedly.dto.response.DashboardResponse;

public interface DashboardService {

    DashboardResponse getDashboard(String userEmail);

}