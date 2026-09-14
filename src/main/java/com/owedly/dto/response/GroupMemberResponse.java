package com.owedly.dto.response;

import java.time.LocalDateTime;

public class GroupMemberResponse {

    private Long userId;
    private String name;
    private String email;
    private LocalDateTime joinedAt;

    public GroupMemberResponse() {
    }

    public GroupMemberResponse(
            Long userId,
            String name,
            String email,
            LocalDateTime joinedAt) {

        this.userId = userId;
        this.name = name;
        this.email = email;
        this.joinedAt = joinedAt;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public LocalDateTime getJoinedAt() {
        return joinedAt;
    }

    public void setJoinedAt(LocalDateTime joinedAt) {
        this.joinedAt = joinedAt;
    }
}