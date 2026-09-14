package com.owedly.controller;

import com.owedly.dto.request.AddGroupMemberRequest;
import com.owedly.dto.request.CreateGroupRequest;
import com.owedly.dto.response.GroupResponse;
import com.owedly.service.GroupService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.core.Authentication;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/groups")
public class GroupController {

    private final GroupService groupService;

    public GroupController(GroupService groupService) {
        this.groupService = groupService;
    }

    @PostMapping
    public ResponseEntity<GroupResponse> createGroup(
            @Valid @RequestBody CreateGroupRequest request,
            Authentication authentication) {

        String userEmail = authentication.getName();

        GroupResponse response =
                groupService.createGroup(request, userEmail);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping
    public ResponseEntity<List<GroupResponse>> getMyGroups(
            Authentication authentication) {

        String userEmail = authentication.getName();

        return ResponseEntity.ok(
                groupService.getMyGroups(userEmail)
        );
    }

    @GetMapping("/{groupId}")
    public ResponseEntity<GroupResponse> getGroup(
            @PathVariable Long groupId,
            Authentication authentication) {

        String userEmail = authentication.getName();

        return ResponseEntity.ok(
                groupService.getGroup(groupId, userEmail)
        );
    }
    
    @PostMapping("/{groupId}/members")
    public ResponseEntity<GroupResponse> addMember(
            @PathVariable Long groupId,
            @Valid @RequestBody AddGroupMemberRequest request,
            Authentication authentication) {

        String userEmail = authentication.getName();

        GroupResponse response =
                groupService.addMember(
                        groupId,
                        request,
                        userEmail
                );

        return ResponseEntity.ok(response);
    }
}