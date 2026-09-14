package com.owedly.service.impl;

import com.owedly.dto.request.AddGroupMemberRequest;
import com.owedly.dto.request.CreateGroupRequest;
import com.owedly.dto.response.GroupMemberResponse;
import com.owedly.dto.response.GroupResponse;
import com.owedly.entity.Group;
import com.owedly.entity.GroupMember;
import com.owedly.entity.User;
import com.owedly.exception.GroupAccessDeniedException;
import com.owedly.exception.ResourceNotFoundException;
import com.owedly.repository.GroupMemberRepository;
import com.owedly.repository.GroupRepository;
import com.owedly.repository.UserRepository;
import com.owedly.service.GroupService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class GroupServiceImpl implements GroupService {

    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final UserRepository userRepository;

    public GroupServiceImpl(
            GroupRepository groupRepository,
            GroupMemberRepository groupMemberRepository,
            UserRepository userRepository) {

        this.groupRepository = groupRepository;
        this.groupMemberRepository = groupMemberRepository;
        this.userRepository = userRepository;
    }

    @Override
    public GroupResponse createGroup(
            CreateGroupRequest request,
            String userEmail) {

        User creator = getUserByEmail(userEmail);

        Group group = new Group();

        group.setName(request.getName().trim());
        group.setDescription(
                request.getDescription() == null
                        ? null
                        : request.getDescription().trim()
        );
        group.setCreatedBy(creator);

        Group savedGroup = groupRepository.save(group);

        // Creator automatically becomes a group member
        GroupMember creatorMembership = new GroupMember();

        creatorMembership.setGroup(savedGroup);
        creatorMembership.setUser(creator);

        groupMemberRepository.save(creatorMembership);

        return buildGroupResponse(savedGroup);
    }

    @Override
    public GroupResponse addMember(
            Long groupId,
            AddGroupMemberRequest request,
            String userEmail) {

        User currentUser = getUserByEmail(userEmail);

        Group group = groupRepository.findById(groupId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Group not found with id: " + groupId
                        )
                );

        // Only existing group members can add another member
        boolean currentUserIsMember =
                groupMemberRepository.existsByGroupIdAndUserId(
                        groupId,
                        currentUser.getId()
                );

        if (!currentUserIsMember) {
            throw new GroupAccessDeniedException(
                    "You are not a member of this group"
            );
        }

        User newMember = userRepository.findById(request.getUserId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found with id: "
                                        + request.getUserId()
                        )
                );

        boolean alreadyMember =
                groupMemberRepository.existsByGroupIdAndUserId(
                        groupId,
                        newMember.getId()
                );

        if (alreadyMember) {
            throw new GroupAccessDeniedException(
                    "User is already a member of this group"
            );
        }

        GroupMember membership = new GroupMember();

        membership.setGroup(group);
        membership.setUser(newMember);

        groupMemberRepository.save(membership);

        return buildGroupResponse(group);
    }

    @Override
    @Transactional(readOnly = true)
    public GroupResponse getGroup(
            Long groupId,
            String userEmail) {

        User currentUser = getUserByEmail(userEmail);

        Group group = groupRepository.findById(groupId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Group not found with id: " + groupId
                        )
                );

        boolean isMember =
                groupMemberRepository.existsByGroupIdAndUserId(
                        groupId,
                        currentUser.getId()
                );

        if (!isMember) {
            throw new IllegalStateException(
                    "You are not a member of this group"
            );
        }

        return buildGroupResponse(group);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GroupResponse> getMyGroups(
            String userEmail) {

        User currentUser = getUserByEmail(userEmail);

        List<GroupMember> memberships =
                groupMemberRepository.findByUserId(
                        currentUser.getId()
                );

        return memberships.stream()
                .map(GroupMember::getGroup)
                .map(this::buildGroupResponse)
                .toList();
    }

    private User getUserByEmail(String email) {

        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found"
                        )
                );
    }

    private GroupResponse buildGroupResponse(Group group) {

        GroupResponse response = new GroupResponse();

        response.setId(group.getId());
        response.setName(group.getName());
        response.setDescription(group.getDescription());

        response.setCreatedBy(
                group.getCreatedBy().getId()
        );

        response.setCreatedByName(
                group.getCreatedBy().getName()
        );

        response.setCreatedAt(group.getCreatedAt());
        response.setUpdatedAt(group.getUpdatedAt());

        List<GroupMemberResponse> members =
                groupMemberRepository
                        .findByGroupId(group.getId())
                        .stream()
                        .map(this::toMemberResponse)
                        .toList();

        response.setMembers(members);

        return response;
    }

    private GroupMemberResponse toMemberResponse(
            GroupMember membership) {

        User user = membership.getUser();

        return new GroupMemberResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                membership.getJoinedAt()
        );
    }
}