package com.owedly.service;

import com.owedly.dto.response.SettlementResponse;

import java.util.List;

public interface SettlementService {

    List<SettlementResponse> getSettlementPlan(
            Long groupId,
            String userEmail
    );
}