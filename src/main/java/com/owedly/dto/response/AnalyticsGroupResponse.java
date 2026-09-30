package com.owedly.dto.response;

import java.math.BigDecimal;

public class AnalyticsGroupResponse {

    private Long groupId;
    private String groupName;
    private BigDecimal amount;

    public AnalyticsGroupResponse() {
    }

    public AnalyticsGroupResponse(
            Long groupId,
            String groupName,
            BigDecimal amount) {

        this.groupId = groupId;
        this.groupName = groupName;
        this.amount = amount;
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

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}