package com.owedly.controller;

import com.owedly.dto.response.SettlementResponse;
import com.owedly.service.SettlementService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/groups")
public class SettlementController {

    private final SettlementService settlementService;

    public SettlementController(SettlementService settlementService) {
        this.settlementService = settlementService;
    }

    @GetMapping("/{groupId}/settlements")
    public ResponseEntity<List<SettlementResponse>> getSettlementPlan(
            @PathVariable Long groupId,
            Authentication authentication) {

        String userEmail = authentication.getName();

        return ResponseEntity.ok(
                settlementService.getSettlementPlan(
                        groupId,
                        userEmail
                )
        );
    }
}