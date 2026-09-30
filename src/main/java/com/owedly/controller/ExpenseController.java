package com.owedly.controller;

import com.owedly.dto.request.CreateExpenseRequest;
import com.owedly.dto.response.ExpenseResponse;
import com.owedly.service.ExpenseService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ExpenseController {

    private final ExpenseService expenseService;

    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }
    
    @DeleteMapping("/expenses/{expenseId}")
    public ResponseEntity<Void> deleteExpense(
            @PathVariable Long expenseId,
            Authentication authentication
    ) {

        String userEmail = authentication.getName();

        expenseService.deleteExpense(
                expenseId,
                userEmail
        );

        return ResponseEntity.noContent().build();
    }

    
    @PutMapping("/expenses/{expenseId}")
    public ResponseEntity<ExpenseResponse> updateExpense(
            @PathVariable Long expenseId,
            @Valid @RequestBody CreateExpenseRequest request,
            Authentication authentication
    ) {

        String userEmail = authentication.getName();

        return ResponseEntity.ok(
                expenseService.updateExpense(
                        expenseId,
                        request,
                        userEmail
                )
        );
    }
    
    @PostMapping("/groups/{groupId}/expenses")
    public ResponseEntity<ExpenseResponse> createExpense(
            @PathVariable Long groupId,
            @Valid @RequestBody CreateExpenseRequest request,
            Authentication authentication) {

        String userEmail = authentication.getName();

        ExpenseResponse response =
                expenseService.createExpense(
                        groupId,
                        request,
                        userEmail
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/groups/{groupId}/expenses")
    public ResponseEntity<List<ExpenseResponse>> getGroupExpenses(
            @PathVariable Long groupId,
            Authentication authentication) {

        String userEmail = authentication.getName();

        return ResponseEntity.ok(
                expenseService.getGroupExpenses(
                        groupId,
                        userEmail
                )
        );
    }

    @GetMapping("/expenses/{expenseId}")
    public ResponseEntity<ExpenseResponse> getExpense(
            @PathVariable Long expenseId,
            Authentication authentication) {

        String userEmail = authentication.getName();

        return ResponseEntity.ok(
                expenseService.getExpense(
                        expenseId,
                        userEmail
                )
        );
    }
}