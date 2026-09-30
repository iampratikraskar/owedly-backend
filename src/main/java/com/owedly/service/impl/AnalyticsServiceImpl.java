package com.owedly.service.impl;

import com.owedly.dto.response.AnalyticsGroupResponse;
import com.owedly.dto.response.AnalyticsMonthlyResponse;
import com.owedly.dto.response.AnalyticsResponse;
import com.owedly.dto.response.AnalyticsSplitMethodResponse;
import com.owedly.entity.User;
import com.owedly.repository.ExpenseRepository;
import com.owedly.repository.UserRepository;
import com.owedly.service.AnalyticsService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class AnalyticsServiceImpl implements AnalyticsService {

    private final ExpenseRepository expenseRepository;
    private final UserRepository userRepository;

    public AnalyticsServiceImpl(
            ExpenseRepository expenseRepository,
            UserRepository userRepository) {

        this.expenseRepository = expenseRepository;
        this.userRepository = userRepository;
    }

    @Override
    public AnalyticsResponse getAnalytics(String userEmail) {

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        Long userId = user.getId();

        BigDecimal totalSpending =
                expenseRepository.sumAmountForUserGroups(userId);

        long totalExpenses =
                expenseRepository.countExpensesForUserGroups(userId);

        if (totalSpending == null) {
            totalSpending = BigDecimal.ZERO;
        }

        BigDecimal averageExpense = BigDecimal.ZERO;

        if (totalExpenses > 0) {
            averageExpense = totalSpending
                    .divide(
                            BigDecimal.valueOf(totalExpenses),
                            2,
                            RoundingMode.HALF_UP
                    );
        }

        List<AnalyticsGroupResponse> spendingByGroup =
                buildSpendingByGroup(userId);

        List<AnalyticsSplitMethodResponse> expensesBySplitMethod =
                buildExpensesBySplitMethod(userId);

        List<AnalyticsMonthlyResponse> monthlySpending =
                buildMonthlySpending(userId);

        AnalyticsResponse response = new AnalyticsResponse();

        response.setTotalSpending(
                totalSpending.setScale(2, RoundingMode.HALF_UP)
        );

        response.setTotalExpenses(totalExpenses);

        response.setAverageExpense(averageExpense);

        response.setSpendingByGroup(spendingByGroup);

        response.setExpensesBySplitMethod(
                expensesBySplitMethod
        );

        response.setMonthlySpending(monthlySpending);

        return response;
    }

    private List<AnalyticsGroupResponse> buildSpendingByGroup(
            Long userId) {

        List<Object[]> results =
                expenseRepository.findSpendingByGroup(userId);

        List<AnalyticsGroupResponse> response =
                new ArrayList<>();

        for (Object[] row : results) {

            Long groupId = ((Number) row[0]).longValue();

            String groupName = (String) row[1];

            BigDecimal amount = (BigDecimal) row[2];

            response.add(
                    new AnalyticsGroupResponse(
                            groupId,
                            groupName,
                            amount.setScale(
                                    2,
                                    RoundingMode.HALF_UP
                            )
                    )
            );
        }

        return response;
    }

    private List<AnalyticsSplitMethodResponse>
    buildExpensesBySplitMethod(Long userId) {

        List<Object[]> results =
                expenseRepository
                        .findExpensesBySplitMethod(userId);

        List<AnalyticsSplitMethodResponse> response =
                new ArrayList<>();

        for (Object[] row : results) {

            String splitMethod =
                    row[0].toString();

            long count =
                    ((Number) row[1]).longValue();

            response.add(
                    new AnalyticsSplitMethodResponse(
                            splitMethod,
                            count
                    )
            );
        }

        return response;
    }

    private List<AnalyticsMonthlyResponse>
    buildMonthlySpending(Long userId) {

        List<Object[]> results =
                expenseRepository
                        .findMonthlySpending(userId);

        List<AnalyticsMonthlyResponse> response =
                new ArrayList<>();

        for (Object[] row : results) {

            int year =
                    ((Number) row[0]).intValue();

            int month =
                    ((Number) row[1]).intValue();

            BigDecimal amount =
                    (BigDecimal) row[2];

            String monthName =
                    Month.of(month)
                            .name()
                            .substring(0, 3);

            String formattedMonth =
                    monthName + " " + year;

            response.add(
                    new AnalyticsMonthlyResponse(
                            formattedMonth,
                            amount.setScale(
                                    2,
                                    RoundingMode.HALF_UP
                            )
                    )
            );
        }

        return response;
    }
}