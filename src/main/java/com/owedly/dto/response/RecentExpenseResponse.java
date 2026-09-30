package com.owedly.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class RecentExpenseResponse {

    private Long id;
    private String description;
    private BigDecimal amount;
    private String splitMethod;
    private LocalDateTime expenseDate;
    private Long groupId;
    private String groupName;
    private String paidByName;

    public RecentExpenseResponse() {
    }

    public RecentExpenseResponse(
            Long id,
            String description,
            BigDecimal amount,
            String splitMethod,
            LocalDateTime expenseDate,
            Long groupId,
            String groupName,
            String paidByName
    ) {
        this.id = id;
        this.description = description;
        this.amount = amount;
        this.splitMethod = splitMethod;
        this.expenseDate = expenseDate;
        this.groupId = groupId;
        this.groupName = groupName;
        this.paidByName = paidByName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getSplitMethod() {
        return splitMethod;
    }

    public void setSplitMethod(String splitMethod) {
        this.splitMethod = splitMethod;
    }

    public LocalDateTime getExpenseDate() {
        return expenseDate;
    }

    public void setExpenseDate(LocalDateTime expenseDate) {
        this.expenseDate = expenseDate;
    }

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getPaidByName() {
        return paidByName;
    }

    public void setPaidByName(String paidByName) {
        this.paidByName = paidByName;
    }
}