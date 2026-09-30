package com.owedly.dto.response;

public class AnalyticsSplitMethodResponse {

    private String splitMethod;
    private long count;

    public AnalyticsSplitMethodResponse() {
    }

    public AnalyticsSplitMethodResponse(
            String splitMethod,
            long count) {

        this.splitMethod = splitMethod;
        this.count = count;
    }

    public String getSplitMethod() {
        return splitMethod;
    }

    public void setSplitMethod(String splitMethod) {
        this.splitMethod = splitMethod;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }
}