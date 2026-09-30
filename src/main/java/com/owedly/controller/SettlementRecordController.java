package com.owedly.controller;

import com.owedly.dto.request.RecordSettlementRequest;
import com.owedly.dto.response.SettlementRecordResponse;
import com.owedly.service.SettlementRecordService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/groups/{groupId}")
public class SettlementRecordController {

    private final SettlementRecordService settlementRecordService;

    public SettlementRecordController(
            SettlementRecordService settlementRecordService
    ) {
        this.settlementRecordService = settlementRecordService;
    }

    @PostMapping("/settlements")
    public ResponseEntity<SettlementRecordResponse> recordSettlement(
            @PathVariable Long groupId,
            @Valid @RequestBody RecordSettlementRequest request,
            Authentication authentication
    ) {

        SettlementRecordResponse response =
                settlementRecordService.recordSettlement(
                        groupId,
                        request,
                        authentication.getName()
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/settlement-history")
    public ResponseEntity<List<SettlementRecordResponse>>
    getSettlementHistory(
            @PathVariable Long groupId,
            Authentication authentication
    ) {

        return ResponseEntity.ok(
                settlementRecordService.getSettlementHistory(
                        groupId,
                        authentication.getName()
                )
        );
    }
}