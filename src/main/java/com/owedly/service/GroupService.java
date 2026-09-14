package com.owedly.service;

import com.owedly.dto.request.AddGroupMemberRequest;
import com.owedly.dto.request.CreateGroupRequest;
import com.owedly.dto.response.GroupResponse;

import java.util.List;

public interface GroupService {

    GroupResponse createGroup(
            CreateGroupRequest request,
            String userEmail
    );

    GroupResponse addMember(
            Long groupId,
            AddGroupMemberRequest request,
            String userEmail
    );

    GroupResponse getGroup(
            Long groupId,
            String userEmail
    );

    List<GroupResponse> getMyGroups(
            String userEmail
    );
}