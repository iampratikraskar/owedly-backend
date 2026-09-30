package com.owedly.dto.response;

import java.math.BigDecimal;
import java.util.List;

public class DashboardResponse {

    private long totalGroups;

    private long totalExpenses;

    private BigDecimal totalSpending;

    private BigDecimal netBalance;
    
    private List<RecentExpenseResponse> recentExpenses;

    public DashboardResponse() {
    }

    public DashboardResponse(
            long totalGroups,
            long totalExpenses,
            BigDecimal totalSpending,
            BigDecimal netBalance,
            List<RecentExpenseResponse> recentExpense
    ) {
        this.totalGroups = totalGroups;
        this.totalExpenses = totalExpenses;
        this.totalSpending = totalSpending;
        this.netBalance = netBalance;
        this.recentExpenses = recentExpense;
    }

    public List<RecentExpenseResponse> getRecentExpenses() {
		return recentExpenses;
	}

	public void setRecentExpenses(List<RecentExpenseResponse> recentExpenses) {
		this.recentExpenses = recentExpenses;
	}

	public long getTotalGroups() {
        return totalGroups;
    }

    public void setTotalGroups(long totalGroups) {
        this.totalGroups = totalGroups;
    }

    public long getTotalExpenses() {
        return totalExpenses;
    }

    public void setTotalExpenses(long totalExpenses) {
        this.totalExpenses = totalExpenses;
    }

    public BigDecimal getTotalSpending() {
        return totalSpending;
    }

    public void setTotalSpending(BigDecimal totalSpending) {
        this.totalSpending = totalSpending;
    }

    public BigDecimal getNetBalance() {
        return netBalance;
    }

    public void setNetBalance(BigDecimal netBalance) {
        this.netBalance = netBalance;
    }
}