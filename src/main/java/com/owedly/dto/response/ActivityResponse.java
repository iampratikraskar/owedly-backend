package com.owedly.dto.response;

import com.owedly.entity.ActivityType;

import java.time.LocalDateTime;

public class ActivityResponse {

    private Long id;
    private Long userId;
    private String userName;
    private Long groupId;
    private String groupName;
    private ActivityType activityType;
    private String message;
    private Long referenceId;
    private LocalDateTime createdAt;

    public ActivityResponse() {
    }

    public ActivityResponse(
            Long id,
            Long userId,
            String userName,
            Long groupId,
            String groupName,
            ActivityType activityType,
            String message,
            Long referenceId,
            LocalDateTime createdAt
    ) {
        this.id = id;
        this.userId = userId;
        this.userName = userName;
        this.groupId = groupId;
        this.groupName = groupName;
        this.activityType = activityType;
        this.message = message;
        this.referenceId = referenceId;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public ActivityType getActivityType() {
        return activityType;
    }

    public void setActivityType(ActivityType activityType) {
        this.activityType = activityType;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Long getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(Long referenceId) {
        this.referenceId = referenceId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}