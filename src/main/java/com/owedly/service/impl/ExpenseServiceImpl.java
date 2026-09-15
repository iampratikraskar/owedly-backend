package com.owedly.service.impl;

import com.owedly.dto.request.CreateExpenseRequest;
import com.owedly.dto.request.ExpenseSplitRequest;
import com.owedly.dto.response.ExpenseResponse;
import com.owedly.dto.response.ExpenseSplitResponse;
import com.owedly.entity.Category;
import com.owedly.entity.Expense;
import com.owedly.entity.ExpenseSplit;
import com.owedly.entity.Group;
import com.owedly.entity.User;
import com.owedly.entity.SplitMethod;
import com.owedly.exception.GroupAccessDeniedException;
import com.owedly.exception.InvalidExpenseException;
import com.owedly.exception.ResourceNotFoundException;
import com.owedly.repository.CategoryRepository;
import com.owedly.repository.ExpenseRepository;
import com.owedly.repository.ExpenseSplitRepository;
import com.owedly.repository.GroupMemberRepository;
import com.owedly.repository.GroupRepository;
import com.owedly.repository.UserRepository;
import com.owedly.service.ExpenseService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Transactional
public class ExpenseServiceImpl implements ExpenseService {

    private static final int MONEY_SCALE = 2;

    private final ExpenseRepository expenseRepository;
    private final ExpenseSplitRepository expenseSplitRepository;
    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    public ExpenseServiceImpl(
            ExpenseRepository expenseRepository,
            ExpenseSplitRepository expenseSplitRepository,
            GroupRepository groupRepository,
            GroupMemberRepository groupMemberRepository,
            UserRepository userRepository,
            CategoryRepository categoryRepository) {

        this.expenseRepository = expenseRepository;
        this.expenseSplitRepository = expenseSplitRepository;
        this.groupRepository = groupRepository;
        this.groupMemberRepository = groupMemberRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
    }

    @Override
    public ExpenseResponse createExpense(
            Long groupId,
            CreateExpenseRequest request,
            String userEmail) {

        User payer = getUserByEmail(userEmail);

        Group group = getGroup(groupId);

        validateMembership(groupId, payer.getId());

        Category category = null;

        if (request.getCategoryId() != null) {
            category = categoryRepository.findById(
                    request.getCategoryId()
            ).orElseThrow(() ->
                    new ResourceNotFoundException(
                            "Category not found with id: "
                                    + request.getCategoryId()
                    )
            );
        }

        validateSplitParticipants(
                groupId,
                request.getSplits()
        );

        Expense expense = new Expense();

        expense.setGroup(group);
        expense.setPaidBy(payer);
        expense.setCategory(category);
        expense.setDescription(
                request.getDescription().trim()
        );
        expense.setAmount(
                request.getAmount()
                        .setScale(MONEY_SCALE, RoundingMode.HALF_UP)
        );
        expense.setSplitMethod(request.getSplitMethod());
        expense.setExpenseDate(
                request.getExpenseDate() != null
                        ? request.getExpenseDate()
                        : LocalDateTime.now()
        );

        Expense savedExpense =
                expenseRepository.save(expense);

        List<ExpenseSplit> splits =
                buildSplits(savedExpense, request);

        expenseSplitRepository.saveAll(splits);

        return buildExpenseResponse(
                savedExpense,
                splits
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExpenseResponse> getGroupExpenses(
            Long groupId,
            String userEmail) {

        User user = getUserByEmail(userEmail);

        getGroup(groupId);

        validateMembership(groupId, user.getId());

        return expenseRepository
                .findByGroupIdOrderByExpenseDateDesc(groupId)
                .stream()
                .map(this::buildExpenseResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ExpenseResponse getExpense(
            Long expenseId,
            String userEmail) {

        User user = getUserByEmail(userEmail);

        Expense expense = expenseRepository
                .findById(expenseId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Expense not found with id: "
                                        + expenseId
                        )
                );

        validateMembership(
                expense.getGroup().getId(),
                user.getId()
        );

        return buildExpenseResponse(expense);
    }

    private List<ExpenseSplit> buildSplits(
            Expense expense,
            CreateExpenseRequest request) {

        switch (request.getSplitMethod()) {

            case EQUAL:
                validateEqualSplits(request.getSplits());
                return buildEqualSplits(
                        expense,
                        request.getSplits()
                );

            case EXACT:
                validateExactSplits(request.getSplits());
                return buildExactSplits(
                        expense,
                        request.getSplits()
                );

            case PERCENTAGE:
                validatePercentageSplits(request.getSplits());
                return buildPercentageSplits(
                        expense,
                        request.getSplits()
                );

            default:
                throw new InvalidExpenseException(
                        "Unsupported split method"
                );
        }
    }

    private List<ExpenseSplit> buildEqualSplits(
            Expense expense,
            List<ExpenseSplitRequest> splitRequests) {

        int participantCount = splitRequests.size();

        long totalCents = expense.getAmount()
                .movePointRight(2)
                .longValueExact();

        long baseCents = totalCents / participantCount;
        long remainderCents = totalCents % participantCount;

        List<ExpenseSplit> splits = new ArrayList<>();

        for (int i = 0; i < participantCount; i++) {

            long participantCents = baseCents;

            if (i < remainderCents) {
                participantCents++;
            }

            BigDecimal shareAmount =
                    BigDecimal.valueOf(participantCents)
                            .movePointLeft(2)
                            .setScale(2);

            ExpenseSplit split = new ExpenseSplit();

            split.setExpense(expense);
            split.setUser(
                    userRepository.findById(splitRequests.get(i).getUserId())
                            .orElseThrow(() ->
                                    new ResourceNotFoundException("User not found"))
            );
            split.setShareAmount(shareAmount);
            split.setPercentage(null);

            splits.add(split);
        }

        return splits;
    }

    private List<ExpenseSplit> buildExactSplits(
            Expense expense,
            List<ExpenseSplitRequest> requests) {

        BigDecimal total =
                requests.stream()
                        .map(ExpenseSplitRequest::getShareAmount)
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        )
                        .setScale(
                                MONEY_SCALE,
                                RoundingMode.HALF_UP
                        );

        if (total.compareTo(expense.getAmount()) != 0) {

            throw new InvalidExpenseException(
                    "Exact split amounts must total "
                            + expense.getAmount()
            );
        }

        return requests.stream()
                .map(request -> {

                    ExpenseSplit split =
                            new ExpenseSplit();

                    split.setExpense(expense);

                    split.setUser(
                            getUserById(request.getUserId())
                    );

                    split.setShareAmount(
                            request.getShareAmount()
                                    .setScale(
                                            MONEY_SCALE,
                                            RoundingMode.HALF_UP
                                    )
                    );

                    split.setPercentage(null);

                    return split;
                })
                .toList();
    }

    private List<ExpenseSplit> buildPercentageSplits(
            Expense expense,
            List<ExpenseSplitRequest> requests) {

        BigDecimal totalPercentage =
                requests.stream()
                        .map(ExpenseSplitRequest::getPercentage)
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );

        if (totalPercentage.compareTo(
                new BigDecimal("100.00")
        ) != 0) {

            throw new InvalidExpenseException(
                    "Percentage splits must total 100%"
            );
        }

        List<ExpenseSplit> splits =
                new java.util.ArrayList<>();

        BigDecimal allocated =
                BigDecimal.ZERO;

        for (int i = 0; i < requests.size(); i++) {

            ExpenseSplitRequest request =
                    requests.get(i);

            BigDecimal share;

            if (i == requests.size() - 1) {

                /*
                 * Give the final participant the remaining
                 * amount so that the total always equals
                 * the expense exactly.
                 */
                share = expense.getAmount()
                        .subtract(allocated);

            } else {

                share = expense.getAmount()
                        .multiply(request.getPercentage())
                        .divide(
                                new BigDecimal("100"),
                                MONEY_SCALE,
                                RoundingMode.HALF_UP
                        );

                allocated = allocated.add(share);
            }

            ExpenseSplit split =
                    new ExpenseSplit();

            split.setExpense(expense);

            split.setUser(
                    getUserById(request.getUserId())
            );

            split.setPercentage(
                    request.getPercentage()
            );

            split.setShareAmount(
                    share.setScale(
                            MONEY_SCALE,
                            RoundingMode.HALF_UP
                    )
            );

            splits.add(split);
        }

        return splits;
    }

    private void validateSplitParticipants(
            Long groupId,
            List<ExpenseSplitRequest> requests) {

        Set<Long> userIds = new HashSet<>();

        for (ExpenseSplitRequest request : requests) {

            Long userId = request.getUserId();

            if (!userIds.add(userId)) {

                throw new InvalidExpenseException(
                        "A user cannot appear more than once "
                                + "in an expense split"
                );
            }

            boolean member =
                    groupMemberRepository
                            .existsByGroupIdAndUserId(
                                    groupId,
                                    userId
                            );

            if (!member) {

                throw new GroupAccessDeniedException(
                        "User with id " + userId
                                + " is not a member of this group"
                );
            }
        }
    }

    private void validateMembership(
            Long groupId,
            Long userId) {

        boolean member =
                groupMemberRepository
                        .existsByGroupIdAndUserId(
                                groupId,
                                userId
                        );

        if (!member) {

            throw new GroupAccessDeniedException(
                    "You are not a member of this group"
            );
        }
    }

    private Group getGroup(Long groupId) {

        return groupRepository.findById(groupId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Group not found with id: "
                                        + groupId
                        )
                );
    }

    private User getUserByEmail(String email) {

        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found"
                        )
                );
    }

    private User getUserById(Long userId) {

        return userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found with id: "
                                        + userId
                        )
                );
    }

    private ExpenseResponse buildExpenseResponse(
            Expense expense) {

        List<ExpenseSplit> splits =
                expenseSplitRepository
                        .findByExpenseId(expense.getId());

        return buildExpenseResponse(
                expense,
                splits
        );
    }

    private ExpenseResponse buildExpenseResponse(
            Expense expense,
            List<ExpenseSplit> splits) {

        ExpenseResponse response =
                new ExpenseResponse();

        response.setId(expense.getId());

        response.setGroupId(
                expense.getGroup().getId()
        );

        response.setDescription(
                expense.getDescription()
        );

        response.setAmount(
                expense.getAmount()
        );

        response.setPaidBy(
                expense.getPaidBy().getId()
        );

        response.setPaidByName(
                expense.getPaidBy().getName()
        );

        if (expense.getCategory() != null) {

            response.setCategoryId(
                    expense.getCategory().getId()
            );

            response.setCategoryName(
                    expense.getCategory().getName()
            );
        }

        response.setSplitMethod(
                expense.getSplitMethod()
        );

        response.setExpenseDate(
                expense.getExpenseDate()
        );

        response.setCreatedAt(
                expense.getCreatedAt()
        );

        response.setUpdatedAt(
                expense.getUpdatedAt()
        );

        List<ExpenseSplitResponse> splitResponses =
                splits.stream()
                        .map(split ->
                                new ExpenseSplitResponse(
                                        split.getUser().getId(),
                                        split.getUser().getName(),
                                        split.getShareAmount(),
                                        split.getPercentage()
                                )
                        )
                        .toList();

        response.setSplits(splitResponses);

        return response;
    }
    
    private void validateEqualSplits(List<ExpenseSplitRequest> splits) {

        for (ExpenseSplitRequest split : splits) {

            if (split.getShareAmount() != null) {
                throw new InvalidExpenseException(
                        "Share amount must not be provided for EQUAL split"
                );
            }

            if (split.getPercentage() != null) {
                throw new InvalidExpenseException(
                        "Percentage must not be provided for EQUAL split"
                );
            }
        }
    }
    
    private void validateExactSplits(List<ExpenseSplitRequest> splits) {

        for (ExpenseSplitRequest split : splits) {

            if (split.getShareAmount() == null) {
                throw new InvalidExpenseException(
                        "Share amount is required for EXACT split"
                );
            }

            if (split.getPercentage() != null) {
                throw new InvalidExpenseException(
                        "Percentage must not be provided for EXACT split"
                );
            }
        }
    }
    
    private void validatePercentageSplits(List<ExpenseSplitRequest> splits) {

        for (ExpenseSplitRequest split : splits) {

            if (split.getPercentage() == null) {
                throw new InvalidExpenseException(
                        "Percentage is required for PERCENTAGE split"
                );
            }

            if (split.getShareAmount() != null) {
                throw new InvalidExpenseException(
                        "Share amount must not be provided for PERCENTAGE split"
                );
            }
        }
    }
    
    
    
    
    
}