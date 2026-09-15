package com.owedly.dto.response;

import java.math.BigDecimal;

public class ExpenseSplitResponse {

    private Long userId;
    private String userName;
    private BigDecimal shareAmount;
    private BigDecimal percentage;

    public ExpenseSplitResponse() {
    }

    public ExpenseSplitResponse(
            Long userId,
            String userName,
            BigDecimal shareAmount,
            BigDecimal percentage) {

        this.userId = userId;
        this.userName = userName;
        this.shareAmount = shareAmount;
        this.percentage = percentage;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public BigDecimal getShareAmount() {
        return shareAmount;
    }

    public void setShareAmount(BigDecimal shareAmount) {
        this.shareAmount = shareAmount;
    }

    public BigDecimal getPercentage() {
        return percentage;
    }

    public void setPercentage(BigDecimal percentage) {
        this.percentage = percentage;
    }
}