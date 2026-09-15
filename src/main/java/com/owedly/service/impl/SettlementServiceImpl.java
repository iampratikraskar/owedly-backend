package com.owedly.service.impl;

import com.owedly.dto.response.BalanceResponse;
import com.owedly.dto.response.SettlementResponse;
import com.owedly.service.BalanceService;
import com.owedly.service.SettlementService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@Transactional
public class SettlementServiceImpl implements SettlementService {

    private static final int SCALE = 2;

    private final BalanceService balanceService;

    public SettlementServiceImpl(BalanceService balanceService) {
        this.balanceService = balanceService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<SettlementResponse> getSettlementPlan(
            Long groupId,
            String userEmail) {

        List<BalanceResponse> balances =
                balanceService.getGroupBalances(groupId, userEmail);

        List<WorkingBalance> creditors = new ArrayList<>();
        List<WorkingBalance> debtors = new ArrayList<>();

        for (BalanceResponse balance : balances) {

            BigDecimal netBalance =
                    normalize(balance.getNetBalance());

            if (netBalance.compareTo(BigDecimal.ZERO) > 0) {

                creditors.add(
                        new WorkingBalance(
                                balance.getUserId(),
                                balance.getUserName(),
                                netBalance
                        )
                );

            } else if (netBalance.compareTo(BigDecimal.ZERO) < 0) {

                debtors.add(
                        new WorkingBalance(
                                balance.getUserId(),
                                balance.getUserName(),
                                netBalance.abs()
                        )
                );
            }
        }

        /*
         * Largest creditors first.
         */
        creditors.sort(
                Comparator.comparing(
                        WorkingBalance::getAmount
                ).reversed()
        );

        /*
         * Largest debtors first.
         */
        debtors.sort(
                Comparator.comparing(
                        WorkingBalance::getAmount
                ).reversed()
        );

        List<SettlementResponse> settlements =
                new ArrayList<>();

        int creditorIndex = 0;
        int debtorIndex = 0;

        while (
                creditorIndex < creditors.size()
                        &&
                debtorIndex < debtors.size()
        ) {

            WorkingBalance creditor =
                    creditors.get(creditorIndex);

            WorkingBalance debtor =
                    debtors.get(debtorIndex);

            BigDecimal settlementAmount =
                    creditor.getAmount()
                            .min(debtor.getAmount())
                            .setScale(
                                    SCALE,
                                    RoundingMode.HALF_UP
                            );

            if (settlementAmount.compareTo(BigDecimal.ZERO) > 0) {

                settlements.add(
                        new SettlementResponse(
                                debtor.getUserId(),
                                debtor.getUserName(),
                                creditor.getUserId(),
                                creditor.getUserName(),
                                settlementAmount
                        )
                );
            }

            creditor.setAmount(
                    normalize(
                            creditor.getAmount()
                                    .subtract(settlementAmount)
                    )
            );

            debtor.setAmount(
                    normalize(
                            debtor.getAmount()
                                    .subtract(settlementAmount)
                    )
            );

            if (creditor.getAmount()
                    .compareTo(BigDecimal.ZERO) == 0) {

                creditorIndex++;
            }

            if (debtor.getAmount()
                    .compareTo(BigDecimal.ZERO) == 0) {

                debtorIndex++;
            }
        }

        return settlements;
    }

    private BigDecimal normalize(BigDecimal value) {

        return value.setScale(
                SCALE,
                RoundingMode.HALF_UP
        );
    }

    private static class WorkingBalance {

        private final Long userId;
        private final String userName;
        private BigDecimal amount;

        public WorkingBalance(
                Long userId,
                String userName,
                BigDecimal amount) {

            this.userId = userId;
            this.userName = userName;
            this.amount = amount;
        }

        public Long getUserId() {
            return userId;
        }

        public String getUserName() {
            return userName;
        }

        public BigDecimal getAmount() {
            return amount;
        }

        public void setAmount(BigDecimal amount) {
            this.amount = amount;
        }
    }
}