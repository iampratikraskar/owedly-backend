package com.owedly.service.impl;

import com.owedly.dto.response.BalanceResponse;
import com.owedly.entity.Expense;
import com.owedly.entity.ExpenseSplit;
import com.owedly.entity.Group;
import com.owedly.entity.GroupMember;
import com.owedly.entity.User;
import com.owedly.exception.GroupAccessDeniedException;
import com.owedly.repository.ExpenseRepository;
import com.owedly.repository.ExpenseSplitRepository;
import com.owedly.repository.GroupMemberRepository;
import com.owedly.repository.GroupRepository;
import com.owedly.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BalanceServiceImplTest {

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private GroupMemberRepository groupMemberRepository;

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private ExpenseSplitRepository expenseSplitRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private BalanceServiceImpl balanceService;

    private Group group;

    private User user1;
    private User user2;
    private User user3;

    private GroupMember member1;
    private GroupMember member2;
    private GroupMember member3;

    @BeforeEach
    void setUp() {

        group = mock(Group.class);

        user1 = mock(User.class);
        user2 = mock(User.class);
        user3 = mock(User.class);

        member1 = mock(GroupMember.class);
        member2 = mock(GroupMember.class);
        member3 = mock(GroupMember.class);
    }

    @Test
    void shouldCalculateBalancesCorrectly() {

        // --------------------------------------------------
        // Expense
        // --------------------------------------------------

        Expense expense = mock(Expense.class);

        // --------------------------------------------------
        // Users
        // --------------------------------------------------

        when(user1.getId()).thenReturn(1L);
        when(user1.getName()).thenReturn("Pratik");

        when(user2.getId()).thenReturn(2L);
        when(user2.getName()).thenReturn("Amit");

        when(user3.getId()).thenReturn(3L);
        when(user3.getName()).thenReturn("Rahul");

        // --------------------------------------------------
        // Group Members
        // --------------------------------------------------

        when(member1.getUser()).thenReturn(user1);
        when(member2.getUser()).thenReturn(user2);
        when(member3.getUser()).thenReturn(user3);

        // --------------------------------------------------
        // Expense details
        // --------------------------------------------------

        when(expense.getId()).thenReturn(1L);

        when(expense.getPaidBy())
                .thenReturn(user1);

        when(expense.getAmount())
                .thenReturn(new BigDecimal("900.00"));

        // --------------------------------------------------
        // Expense Splits
        // --------------------------------------------------

        ExpenseSplit split1 = mock(ExpenseSplit.class);
        ExpenseSplit split2 = mock(ExpenseSplit.class);
        ExpenseSplit split3 = mock(ExpenseSplit.class);

        // IMPORTANT:
        // BalanceServiceImpl uses split.getExpense().getId()

        when(split1.getExpense())
                .thenReturn(expense);

        when(split2.getExpense())
                .thenReturn(expense);

        when(split3.getExpense())
                .thenReturn(expense);

        when(split1.getUser())
                .thenReturn(user1);

        when(split1.getShareAmount())
                .thenReturn(new BigDecimal("300.00"));

        when(split2.getUser())
                .thenReturn(user2);

        when(split2.getShareAmount())
                .thenReturn(new BigDecimal("300.00"));

        when(split3.getUser())
                .thenReturn(user3);

        when(split3.getShareAmount())
                .thenReturn(new BigDecimal("300.00"));

        // --------------------------------------------------
        // Repository mocking
        // --------------------------------------------------

        when(groupRepository.findById(1L))
                .thenReturn(Optional.of(group));

        when(userRepository.findByEmail("pratik@gmail.com"))
                .thenReturn(Optional.of(user1));

        when(groupMemberRepository.existsByGroupIdAndUserId(1L, 1L))
                .thenReturn(true);

        when(groupMemberRepository.findByGroupId(1L))
                .thenReturn(List.of(
                        member1,
                        member2,
                        member3
                ));

        when(expenseRepository.findByGroupIdOrderByExpenseDateDesc(1L))
                .thenReturn(List.of(expense));

        when(expenseSplitRepository.findByExpenseGroupId(1L))
                .thenReturn(List.of(
                        split1,
                        split2,
                        split3
                ));

        // --------------------------------------------------
        // Execute
        // --------------------------------------------------

        List<BalanceResponse> result =
                balanceService.getGroupBalances(
                        1L,
                        "pratik@gmail.com"
                );

        // --------------------------------------------------
        // Basic validation
        // --------------------------------------------------

        assertEquals(3, result.size());

        // --------------------------------------------------
        // Pratik
        // Paid: ₹900
        // Share: ₹300
        // Net: +₹600
        // --------------------------------------------------

        assertEquals(
                new BigDecimal("900.00"),
                result.get(0).getTotalPaid()
        );

        assertEquals(
                new BigDecimal("300.00"),
                result.get(0).getTotalShare()
        );

        assertEquals(
                new BigDecimal("600.00"),
                result.get(0).getNetBalance()
        );

        // --------------------------------------------------
        // Amit
        // Paid: ₹0
        // Share: ₹300
        // Net: -₹300
        // --------------------------------------------------

        assertEquals(
                0,
                result.get(1).getTotalPaid().compareTo(BigDecimal.ZERO)
        );

        assertEquals(
                new BigDecimal("300.00"),
                result.get(1).getTotalShare()
        );

        assertEquals(
                new BigDecimal("-300.00"),
                result.get(1).getNetBalance()
        );

        // --------------------------------------------------
        // Rahul
        // Paid: ₹0
        // Share: ₹300
        // Net: -₹300
        // --------------------------------------------------

        assertEquals(
                0,
                result.get(2).getTotalPaid().compareTo(BigDecimal.ZERO)
        );

        assertEquals(
                new BigDecimal("300.00"),
                result.get(2).getTotalShare()
        );

        assertEquals(
                new BigDecimal("-300.00"),
                result.get(2).getNetBalance()
        );
    }

    @Test
    void shouldMaintainZeroBalanceInvariant() {

        // --------------------------------------------------
        // Expenses
        // --------------------------------------------------

        Expense expense1 = mock(Expense.class);
        Expense expense2 = mock(Expense.class);

        // --------------------------------------------------
        // Users
        // --------------------------------------------------

        when(user1.getId()).thenReturn(1L);
        when(user1.getName()).thenReturn("Pratik");

        when(user2.getId()).thenReturn(2L);
        when(user2.getName()).thenReturn("Amit");

        when(user3.getId()).thenReturn(3L);
        when(user3.getName()).thenReturn("Rahul");

        // --------------------------------------------------
        // Group Members
        // --------------------------------------------------

        when(member1.getUser()).thenReturn(user1);
        when(member2.getUser()).thenReturn(user2);
        when(member3.getUser()).thenReturn(user3);

        // --------------------------------------------------
        // Expense 1
        // Paid by Pratik: ₹900
        // --------------------------------------------------

        when(expense1.getId())
                .thenReturn(1L);

        when(expense1.getPaidBy())
                .thenReturn(user1);

        when(expense1.getAmount())
                .thenReturn(new BigDecimal("900.00"));

        // --------------------------------------------------
        // Expense 2
        // Paid by Amit: ₹600
        // --------------------------------------------------

        when(expense2.getId())
                .thenReturn(2L);

        when(expense2.getPaidBy())
                .thenReturn(user2);

        when(expense2.getAmount())
                .thenReturn(new BigDecimal("600.00"));

        // --------------------------------------------------
        // Splits for Expense 1
        // --------------------------------------------------

        ExpenseSplit e1s1 = mock(ExpenseSplit.class);
        ExpenseSplit e1s2 = mock(ExpenseSplit.class);
        ExpenseSplit e1s3 = mock(ExpenseSplit.class);

        when(e1s1.getExpense())
                .thenReturn(expense1);

        when(e1s2.getExpense())
                .thenReturn(expense1);

        when(e1s3.getExpense())
                .thenReturn(expense1);

        when(e1s1.getUser())
                .thenReturn(user1);

        when(e1s1.getShareAmount())
                .thenReturn(new BigDecimal("300.00"));

        when(e1s2.getUser())
                .thenReturn(user2);

        when(e1s2.getShareAmount())
                .thenReturn(new BigDecimal("300.00"));

        when(e1s3.getUser())
                .thenReturn(user3);

        when(e1s3.getShareAmount())
                .thenReturn(new BigDecimal("300.00"));

        // --------------------------------------------------
        // Splits for Expense 2
        // --------------------------------------------------

        ExpenseSplit e2s1 = mock(ExpenseSplit.class);
        ExpenseSplit e2s2 = mock(ExpenseSplit.class);
        ExpenseSplit e2s3 = mock(ExpenseSplit.class);

        when(e2s1.getExpense())
                .thenReturn(expense2);

        when(e2s2.getExpense())
                .thenReturn(expense2);

        when(e2s3.getExpense())
                .thenReturn(expense2);

        when(e2s1.getUser())
                .thenReturn(user1);

        when(e2s1.getShareAmount())
                .thenReturn(new BigDecimal("200.00"));

        when(e2s2.getUser())
                .thenReturn(user2);

        when(e2s2.getShareAmount())
                .thenReturn(new BigDecimal("200.00"));

        when(e2s3.getUser())
                .thenReturn(user3);

        when(e2s3.getShareAmount())
                .thenReturn(new BigDecimal("200.00"));

        // --------------------------------------------------
        // Repository mocking
        // --------------------------------------------------

        when(groupRepository.findById(1L))
                .thenReturn(Optional.of(group));

        when(userRepository.findByEmail("pratik@gmail.com"))
                .thenReturn(Optional.of(user1));

        when(groupMemberRepository.existsByGroupIdAndUserId(1L, 1L))
                .thenReturn(true);

        when(groupMemberRepository.findByGroupId(1L))
                .thenReturn(List.of(
                        member1,
                        member2,
                        member3
                ));

        when(expenseRepository.findByGroupIdOrderByExpenseDateDesc(1L))
                .thenReturn(List.of(
                        expense1,
                        expense2
                ));

        when(expenseSplitRepository.findByExpenseGroupId(1L))
                .thenReturn(List.of(
                        e1s1,
                        e1s2,
                        e1s3,
                        e2s1,
                        e2s2,
                        e2s3
                ));

        // --------------------------------------------------
        // Execute
        // --------------------------------------------------

        List<BalanceResponse> result =
                balanceService.getGroupBalances(
                        1L,
                        "pratik@gmail.com"
                );

        // --------------------------------------------------
        // Calculate total net balance
        // --------------------------------------------------

        BigDecimal totalNetBalance =
                result.stream()
                        .map(BalanceResponse::getNetBalance)
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );

        // --------------------------------------------------
        // Zero balance invariant
        // --------------------------------------------------

        assertEquals(
                0,
                totalNetBalance.compareTo(BigDecimal.ZERO)
        );
    }

    @Test
    void shouldRejectNonMemberAccess() {

        // --------------------------------------------------
        // Outsider
        // --------------------------------------------------

        when(user1.getId())
                .thenReturn(1L);

        when(groupRepository.findById(1L))
                .thenReturn(Optional.of(group));

        when(userRepository.findByEmail("outsider@gmail.com"))
                .thenReturn(Optional.of(user1));

        when(groupMemberRepository.existsByGroupIdAndUserId(1L, 1L))
                .thenReturn(false);

        // --------------------------------------------------
        // Execute + verify exception
        // --------------------------------------------------

        assertThrows(
                GroupAccessDeniedException.class,
                () -> balanceService.getGroupBalances(
                        1L,
                        "outsider@gmail.com"
                )
        );

        // --------------------------------------------------
        // Expenses should never be queried
        // because user is not a group member.
        // --------------------------------------------------

        verify(
                expenseRepository,
                never()
        ).findByGroupIdOrderByExpenseDateDesc(1L);
    }
}