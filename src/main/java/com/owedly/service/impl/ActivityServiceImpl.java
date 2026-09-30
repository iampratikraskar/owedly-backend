package com.owedly.service.impl;

import com.owedly.dto.response.ActivityResponse;
import com.owedly.entity.ActivityLog;
import com.owedly.entity.Group;
import com.owedly.entity.User;
import com.owedly.exception.GroupAccessDeniedException;
import com.owedly.exception.ResourceNotFoundException;
import com.owedly.repository.ActivityLogRepository;
import com.owedly.repository.GroupMemberRepository;
import com.owedly.repository.GroupRepository;
import com.owedly.repository.UserRepository;
import com.owedly.service.ActivityService;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class ActivityServiceImpl implements ActivityService {

    private final ActivityLogRepository activityLogRepository;
    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;

    public ActivityServiceImpl(
            ActivityLogRepository activityLogRepository,
            UserRepository userRepository,
            GroupRepository groupRepository,
            GroupMemberRepository groupMemberRepository
    ) {
        this.activityLogRepository = activityLogRepository;
        this.userRepository = userRepository;
        this.groupRepository = groupRepository;
        this.groupMemberRepository = groupMemberRepository;
    }

    // =========================================================
    // GET MY ACTIVITIES
    // =========================================================

    @Override
    public List<ActivityResponse> getMyActivities(
            String userEmail,
            int limit
    ) {

        User user = getUserByEmail(userEmail);

        Pageable pageable = PageRequest.of(
                0,
                normalizeLimit(limit)
        );

        return activityLogRepository
                .findByUserIdOrderByCreatedAtDesc(
                        user.getId(),
                        pageable
                )
                .stream()
                .map(this::buildActivityResponse)
                .toList();
    }

    // =========================================================
    // GET GROUP ACTIVITIES
    // =========================================================

    @Override
    public List<ActivityResponse> getGroupActivities(
            Long groupId,
            String userEmail,
            int limit
    ) {

        User user = getUserByEmail(userEmail);

        Group group = groupRepository.findById(groupId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Group not found with id: " + groupId
                        )
                );

        // -----------------------------------------------------
        // Only group members can view group activity
        // -----------------------------------------------------

        boolean member =
                groupMemberRepository.existsByGroupIdAndUserId(
                        groupId,
                        user.getId()
                );

        if (!member) {
            throw new GroupAccessDeniedException(
                    "You are not a member of this group"
            );
        }

        Pageable pageable = PageRequest.of(
                0,
                normalizeLimit(limit)
        );

        return activityLogRepository
                .findByGroupIdOrderByCreatedAtDesc(
                        group.getId(),
                        pageable
                )
                .stream()
                .map(this::buildActivityResponse)
                .toList();
    }

    // =========================================================
    // BUILD RESPONSE
    // =========================================================

    private ActivityResponse buildActivityResponse(
            ActivityLog activity
    ) {

        Long groupId = null;
        String groupName = null;

        if (activity.getGroup() != null) {
            groupId = activity.getGroup().getId();
            groupName = activity.getGroup().getName();
        }

        return new ActivityResponse(
                activity.getId(),
                activity.getUser().getId(),
                activity.getUser().getName(),
                groupId,
                groupName,
                activity.getActivityType(),
                activity.getMessage(),
                activity.getReferenceId(),
                activity.getCreatedAt()
        );
    }

    // =========================================================
    // USER LOOKUP
    // =========================================================

    private User getUserByEmail(String email) {

        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found"
                        )
                );
    }

    // =========================================================
    // LIMIT VALIDATION
    // =========================================================

    private int normalizeLimit(int limit) {

        if (limit <= 0) {
            return 20;
        }

        return Math.min(limit, 100);
    }
}