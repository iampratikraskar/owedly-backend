package com.owedly.service.impl;

import com.owedly.dto.response.BalanceResponse;
import com.owedly.entity.Expense;
import com.owedly.entity.ExpenseSplit;
import com.owedly.entity.Group;
import com.owedly.entity.GroupMember;
import com.owedly.entity.User;
import com.owedly.exception.GroupAccessDeniedException;
import com.owedly.exception.ResourceNotFoundException;
import com.owedly.repository.ExpenseRepository;
import com.owedly.repository.ExpenseSplitRepository;
import com.owedly.repository.GroupMemberRepository;
import com.owedly.repository.GroupRepository;
import com.owedly.repository.UserRepository;
import com.owedly.service.BalanceService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class BalanceServiceImpl implements BalanceService {

    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final ExpenseRepository expenseRepository;
    private final ExpenseSplitRepository expenseSplitRepository;
    private final UserRepository userRepository;

    public BalanceServiceImpl(
            GroupRepository groupRepository,
            GroupMemberRepository groupMemberRepository,
            ExpenseRepository expenseRepository,
            ExpenseSplitRepository expenseSplitRepository,
            UserRepository userRepository) {

        this.groupRepository = groupRepository;
        this.groupMemberRepository = groupMemberRepository;
        this.expenseRepository = expenseRepository;
        this.expenseSplitRepository = expenseSplitRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<BalanceResponse> getGroupBalances(
            Long groupId,
            String userEmail) {

        // 1. Find group
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Group not found"));

        // 2. Find current user
        User currentUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        // 3. Verify current user is a member
        if (!groupMemberRepository.existsByGroupIdAndUserId(
                groupId,
                currentUser.getId())) {

            throw new GroupAccessDeniedException(
                    "You are not a member of this group"
            );
        }

        // 4. Get all group members
        List<GroupMember> members =
                groupMemberRepository.findByGroupId(groupId);

        // 5. Get all group expenses
        List<Expense> expenses =
                expenseRepository.findByGroupIdOrderByExpenseDateDesc(groupId);

        // 6. Initialize balance maps
        Map<Long, BigDecimal> totalPaid = new HashMap<>();
        Map<Long, BigDecimal> totalShare = new HashMap<>();

        for (GroupMember member : members) {

            Long userId = member.getUser().getId();

            totalPaid.put(
                    userId,
                    BigDecimal.ZERO
            );

            totalShare.put(
                    userId,
                    BigDecimal.ZERO
            );
        }

        // 7. Get ALL splits for this group in one query
        List<ExpenseSplit> allSplits =
                expenseSplitRepository.findByExpenseGroupId(groupId);

        // 8. Group splits by expense ID
        Map<Long, List<ExpenseSplit>> splitsByExpense =
                new HashMap<>();

        for (ExpenseSplit split : allSplits) {

            Long expenseId =
                    split.getExpense().getId();

            splitsByExpense
                    .computeIfAbsent(
                            expenseId,
                            key -> new ArrayList<>()
                    )
                    .add(split);
        }

        // 9. Process every expense
        for (Expense expense : expenses) {

            Long payerId =
                    expense.getPaidBy().getId();

            // Add amount paid by payer
            totalPaid.put(
                    payerId,
                    totalPaid.get(payerId)
                            .add(expense.getAmount())
            );

            // Get splits belonging to this expense
            List<ExpenseSplit> splits =
                    splitsByExpense.getOrDefault(
                            expense.getId(),
                            List.of()
                    );

            // Add each member's share
            for (ExpenseSplit split : splits) {

                Long userId =
                        split.getUser().getId();

                totalShare.put(
                        userId,
                        totalShare.get(userId)
                                .add(split.getShareAmount())
                );
            }
        }

        // 10. Build balance response
        List<BalanceResponse> responses =
                new ArrayList<>();

        for (GroupMember member : members) {

            User user =
                    member.getUser();

            Long userId =
                    user.getId();

            BigDecimal paid =
                    totalPaid.get(userId);

            BigDecimal share =
                    totalShare.get(userId);

            BigDecimal netBalance =
                    paid.subtract(share);

            responses.add(
                    new BalanceResponse(
                            userId,
                            user.getName(),
                            paid,
                            share,
                            netBalance
                    )
            );
        }

        return responses;
    }
}