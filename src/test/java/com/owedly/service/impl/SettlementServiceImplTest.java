package com.owedly.service.impl;

import com.owedly.dto.response.BalanceResponse;
import com.owedly.dto.response.SettlementResponse;
import com.owedly.service.BalanceService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SettlementServiceImplTest {

    @Mock
    private BalanceService balanceService;

    @InjectMocks
    private SettlementServiceImpl settlementService;

    @Test
    void shouldSimplifySingleCreditorAndMultipleDebtors() {

        // Pratik +600
        // Amit   -300
        // Rahul  -300

        List<BalanceResponse> balances = List.of(
                new BalanceResponse(
                        1L,
                        "Pratik",
                        new BigDecimal("900.00"),
                        new BigDecimal("300.00"),
                        new BigDecimal("600.00")
                ),
                new BalanceResponse(
                        2L,
                        "Amit",
                        BigDecimal.ZERO,
                        new BigDecimal("300.00"),
                        new BigDecimal("-300.00")
                ),
                new BalanceResponse(
                        3L,
                        "Rahul",
                        BigDecimal.ZERO,
                        new BigDecimal("300.00"),
                        new BigDecimal("-300.00")
                )
        );

        when(balanceService.getGroupBalances(
                1L,
                "pratik@gmail.com"
        )).thenReturn(balances);

        List<SettlementResponse> result =
                settlementService.getSettlementPlan(
                        1L,
                        "pratik@gmail.com"
                );

        assertEquals(2, result.size());

        assertEquals(
                "Amit",
                result.get(0).getFromUserName()
        );

        assertEquals(
                "Pratik",
                result.get(0).getToUserName()
        );

        assertEquals(
                new BigDecimal("300.00"),
                result.get(0).getAmount()
        );

        assertEquals(
                "Rahul",
                result.get(1).getFromUserName()
        );

        assertEquals(
                "Pratik",
                result.get(1).getToUserName()
        );

        assertEquals(
                new BigDecimal("300.00"),
                result.get(1).getAmount()
        );
    }

    @Test
    void shouldSimplifyMultipleCreditorsAndMultipleDebtors() {

        // A +700
        // B +300
        // C -500
        // D -500

        List<BalanceResponse> balances = List.of(
                new BalanceResponse(
                        1L,
                        "A",
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        new BigDecimal("700.00")
                ),
                new BalanceResponse(
                        2L,
                        "B",
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        new BigDecimal("300.00")
                ),
                new BalanceResponse(
                        3L,
                        "C",
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        new BigDecimal("-500.00")
                ),
                new BalanceResponse(
                        4L,
                        "D",
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        new BigDecimal("-500.00")
                )
        );

        when(balanceService.getGroupBalances(
                1L,
                "a@gmail.com"
        )).thenReturn(balances);

        List<SettlementResponse> result =
                settlementService.getSettlementPlan(
                        1L,
                        "a@gmail.com"
                );

        assertEquals(3, result.size());

        assertEquals(
                new BigDecimal("500.00"),
                result.get(0).getAmount()
        );

        assertEquals(
                new BigDecimal("200.00"),
                result.get(1).getAmount()
        );

        assertEquals(
                new BigDecimal("300.00"),
                result.get(2).getAmount()
        );
    }

    @Test
    void shouldReturnEmptyPlanWhenAllBalancesAreZero() {

        List<BalanceResponse> balances = List.of(
                new BalanceResponse(
                        1L,
                        "Pratik",
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO
                ),
                new BalanceResponse(
                        2L,
                        "Amit",
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO
                )
        );

        when(balanceService.getGroupBalances(
                1L,
                "pratik@gmail.com"
        )).thenReturn(balances);

        List<SettlementResponse> result =
                settlementService.getSettlementPlan(
                        1L,
                        "pratik@gmail.com"
                );

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldHandleSingleDebtorAndSingleCreditor() {

        // Pratik +1000
        // Amit   -1000

        List<BalanceResponse> balances = List.of(
                new BalanceResponse(
                        1L,
                        "Pratik",
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        new BigDecimal("1000.00")
                ),
                new BalanceResponse(
                        2L,
                        "Amit",
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        new BigDecimal("-1000.00")
                )
        );

        when(balanceService.getGroupBalances(
                1L,
                "pratik@gmail.com"
        )).thenReturn(balances);

        List<SettlementResponse> result =
                settlementService.getSettlementPlan(
                        1L,
                        "pratik@gmail.com"
                );

        assertEquals(1, result.size());

        SettlementResponse settlement =
                result.get(0);

        assertEquals(
                "Amit",
                settlement.getFromUserName()
        );

        assertEquals(
                "Pratik",
                settlement.getToUserName()
        );

        assertEquals(
                new BigDecimal("1000.00"),
                settlement.getAmount()
        );
    }

    @Test
    void shouldCallBalanceServiceWithCorrectArguments() {

        List<BalanceResponse> balances = List.of(
                new BalanceResponse(
                        1L,
                        "Pratik",
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO
                )
        );

        when(balanceService.getGroupBalances(
                10L,
                "pratik@gmail.com"
        )).thenReturn(balances);

        settlementService.getSettlementPlan(
                10L,
                "pratik@gmail.com"
        );

        verify(balanceService)
                .getGroupBalances(
                        10L,
                        "pratik@gmail.com"
                );
    }
}