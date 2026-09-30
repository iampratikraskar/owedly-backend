package com.owedly.service;

import com.owedly.dto.request.RecordSettlementRequest;
import com.owedly.dto.response.SettlementRecordResponse;

import java.util.List;

public interface SettlementRecordService {

    SettlementRecordResponse recordSettlement(
            Long groupId,
            RecordSettlementRequest request,
            String userEmail
    );

    List<SettlementRecordResponse> getSettlementHistory(
            Long groupId,
            String userEmail
    );
}