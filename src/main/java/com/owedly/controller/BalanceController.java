package com.owedly.controller;

import com.owedly.dto.response.BalanceResponse;
import com.owedly.service.BalanceService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/groups")
public class BalanceController {

    private final BalanceService balanceService;

    public BalanceController(BalanceService balanceService) {
        this.balanceService = balanceService;
    }

    @GetMapping("/{groupId}/balances")
    public ResponseEntity<List<BalanceResponse>> getGroupBalances(
            @PathVariable Long groupId,
            Authentication authentication) {

        String userEmail = authentication.getName();

        return ResponseEntity.ok(
                balanceService.getGroupBalances(
                        groupId,
                        userEmail
                )
        );
    }
}