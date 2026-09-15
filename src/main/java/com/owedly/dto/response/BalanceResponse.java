package com.owedly.dto.response;

import java.math.BigDecimal;

public class BalanceResponse {

    private Long userId;
    private String userName;

    private BigDecimal totalPaid;
    private BigDecimal totalShare;
    private BigDecimal netBalance;

    public BalanceResponse() {
    }

    public BalanceResponse(
            Long userId,
            String userName,
            BigDecimal totalPaid,
            BigDecimal totalShare,
            BigDecimal netBalance) {

        this.userId = userId;
        this.userName = userName;
        this.totalPaid = totalPaid;
        this.totalShare = totalShare;
        this.netBalance = netBalance;
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

    public BigDecimal getTotalPaid() {
        return totalPaid;
    }

    public void setTotalPaid(BigDecimal totalPaid) {
        this.totalPaid = totalPaid;
    }

    public BigDecimal getTotalShare() {
        return totalShare;
    }

    public void setTotalShare(BigDecimal totalShare) {
        this.totalShare = totalShare;
    }

    public BigDecimal getNetBalance() {
        return netBalance;
    }

    public void setNetBalance(BigDecimal netBalance) {
        this.netBalance = netBalance;
    }
}