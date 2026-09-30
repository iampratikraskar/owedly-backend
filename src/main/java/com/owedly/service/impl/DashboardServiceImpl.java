package com.owedly.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.owedly.dto.response.BalanceResponse;
import com.owedly.dto.response.DashboardResponse;
import com.owedly.dto.response.RecentExpenseResponse;
import com.owedly.entity.Expense;
import com.owedly.entity.GroupMember;
import com.owedly.entity.User;
import com.owedly.exception.ResourceNotFoundException;
import com.owedly.repository.ExpenseRepository;
import com.owedly.repository.GroupMemberRepository;
import com.owedly.repository.UserRepository;
import com.owedly.service.BalanceService;
import com.owedly.service.DashboardService;

@Service
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

    private final UserRepository userRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final ExpenseRepository expenseRepository;
    private final BalanceService balanceService;

    public DashboardServiceImpl(
            UserRepository userRepository,
            GroupMemberRepository groupMemberRepository,
            ExpenseRepository expenseRepository,
            BalanceService balanceService
    ) {
        this.userRepository = userRepository;
        this.groupMemberRepository = groupMemberRepository;
        this.expenseRepository = expenseRepository;
        this.balanceService = balanceService;
    }

    @Override
    public DashboardResponse getDashboard(String userEmail) {

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found")
                );

        long totalGroups =
                groupMemberRepository.countByUserId(user.getId());

        long totalExpenses =
                expenseRepository.countExpensesForUserGroups(
                        user.getId()
                );

        BigDecimal totalSpending =
                expenseRepository.sumExpensesForUserGroups(
                        user.getId()
                );

        if (totalSpending == null) {
            totalSpending = BigDecimal.ZERO;
        }

        // Calculate user's net balance across all groups
        BigDecimal netBalance = BigDecimal.ZERO;

        List<GroupMember> memberships =
                groupMemberRepository.findByUserId(user.getId());

        for (GroupMember membership : memberships) {

            List<BalanceResponse> balances =
                    balanceService.getGroupBalances(
                            membership.getGroup().getId(),
                            userEmail
                    );

            for (BalanceResponse balance : balances) {

                if (balance.getUserId().equals(user.getId())) {

                    netBalance = netBalance.add(
                            balance.getNetBalance()
                    );

                    break;
                }
            }
        }

        // Get latest 5 expenses
        List<Expense> recentExpenses =
                expenseRepository.findRecentExpensesForUserGroups(
                        user.getId(),
                        PageRequest.of(0, 5)
                );

        List<RecentExpenseResponse> recentExpenseResponses =
                new ArrayList<>();

        for (Expense expense : recentExpenses) {

            recentExpenseResponses.add(
                    new RecentExpenseResponse(
                            expense.getId(),
                            expense.getDescription(),
                            expense.getAmount(),
                            expense.getSplitMethod().name(),
                            expense.getExpenseDate(),
                            expense.getGroup().getId(),
                            expense.getGroup().getName(),
                            expense.getPaidBy().getName()
                    )
            );
        }

        return new DashboardResponse(
                totalGroups,
                totalExpenses,
                totalSpending,
                netBalance,
                recentExpenseResponses
        );
    }
}