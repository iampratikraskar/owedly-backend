package com.owedly.service;

import com.owedly.dto.response.BalanceResponse;

import java.util.List;

public interface BalanceService {

    List<BalanceResponse> getGroupBalances(
            Long groupId,
            String userEmail
    );
}