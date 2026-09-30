package com.owedly.dto.request;

public class AnalyticsRequest {

    private String period;

    public AnalyticsRequest() {
    }

    public AnalyticsRequest(String period) {
        this.period = period;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }
}