package com.owedly.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public class GroupResponse {

    private Long id;
    private String name;
    private String description;

    private Long createdBy;
    private String createdByName;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private List<GroupMemberResponse> members;

    public GroupResponse() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    public String getCreatedByName() {
        return createdByName;
    }

    public void setCreatedByName(String createdByName) {
        this.createdByName = createdByName;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<GroupMemberResponse> getMembers() {
        return members;
    }

    public void setMembers(List<GroupMemberResponse> members) {
        this.members = members;
    }
}