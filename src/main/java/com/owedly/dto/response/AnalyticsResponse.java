package com.owedly.dto.response;

import java.math.BigDecimal;
import java.util.List;

public class AnalyticsResponse {

    private BigDecimal totalSpending;
    private long totalExpenses;
    private BigDecimal averageExpense;

    private List<AnalyticsGroupResponse> spendingByGroup;
    private List<AnalyticsSplitMethodResponse> expensesBySplitMethod;
    private List<AnalyticsMonthlyResponse> monthlySpending;

    public AnalyticsResponse() {
    }

    public BigDecimal getTotalSpending() {
        return totalSpending;
    }

    public void setTotalSpending(BigDecimal totalSpending) {
        this.totalSpending = totalSpending;
    }

    public long getTotalExpenses() {
        return totalExpenses;
    }

    public void setTotalExpenses(long totalExpenses) {
        this.totalExpenses = totalExpenses;
    }

    public BigDecimal getAverageExpense() {
        return averageExpense;
    }

    public void setAverageExpense(BigDecimal averageExpense) {
        this.averageExpense = averageExpense;
    }

    public List<AnalyticsGroupResponse> getSpendingByGroup() {
        return spendingByGroup;
    }

    public void setSpendingByGroup(List<AnalyticsGroupResponse> spendingByGroup) {
        this.spendingByGroup = spendingByGroup;
    }

    public List<AnalyticsSplitMethodResponse> getExpensesBySplitMethod() {
        return expensesBySplitMethod;
    }

    public void setExpensesBySplitMethod(
            List<AnalyticsSplitMethodResponse> expensesBySplitMethod) {
        this.expensesBySplitMethod = expensesBySplitMethod;
    }

    public List<AnalyticsMonthlyResponse> getMonthlySpending() {
        return monthlySpending;
    }

    public void setMonthlySpending(
            List<AnalyticsMonthlyResponse> monthlySpending) {
        this.monthlySpending = monthlySpending;
    }
}