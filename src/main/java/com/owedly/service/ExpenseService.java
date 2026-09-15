package com.owedly.service;

import com.owedly.dto.request.CreateExpenseRequest;
import com.owedly.dto.response.ExpenseResponse;

import java.util.List;

public interface ExpenseService {

    ExpenseResponse createExpense(
            Long groupId,
            CreateExpenseRequest request,
            String userEmail
    );

    List<ExpenseResponse> getGroupExpenses(
            Long groupId,
            String userEmail
    );

    ExpenseResponse getExpense(
            Long expenseId,
            String userEmail
    );
}