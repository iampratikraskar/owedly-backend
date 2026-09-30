package com.owedly.service.impl;

import com.owedly.dto.request.RecordSettlementRequest;
import com.owedly.dto.response.SettlementRecordResponse;
import com.owedly.entity.ActivityType;
import com.owedly.entity.Group;
import com.owedly.entity.GroupMember;
import com.owedly.entity.Settlement;
import com.owedly.entity.User;
import com.owedly.exception.GroupAccessDeniedException;
import com.owedly.exception.InvalidExpenseException;
import com.owedly.repository.GroupMemberRepository;
import com.owedly.repository.GroupRepository;
import com.owedly.repository.SettlementRepository;
import com.owedly.repository.UserRepository;
import com.owedly.service.ActivityLogService;
import com.owedly.service.SettlementRecordService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional
public class SettlementRecordServiceImpl implements SettlementRecordService {

    private final SettlementRepository settlementRepository;
    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final UserRepository userRepository;
    private final ActivityLogService activityLogService;

    public SettlementRecordServiceImpl(
            SettlementRepository settlementRepository,
            GroupRepository groupRepository,
            GroupMemberRepository groupMemberRepository,
            UserRepository userRepository,
            ActivityLogService activityLogService
    ) {
        this.settlementRepository = settlementRepository;
        this.groupRepository = groupRepository;
        this.groupMemberRepository = groupMemberRepository;
        this.userRepository = userRepository;
        this.activityLogService = activityLogService;
    }

    @Override
    public SettlementRecordResponse recordSettlement(
            Long groupId,
            RecordSettlementRequest request,
            String userEmail
    ) {

        User currentUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() ->
                        new RuntimeException("User not found")
                );

        Group group = groupRepository.findById(groupId)
                .orElseThrow(() ->
                        new RuntimeException("Group not found")
                );

        validateGroupMembership(groupId, currentUser.getId());

        if (request.getFromUserId().equals(request.getToUserId())) {
            throw new InvalidExpenseException(
                    "A user cannot settle with themselves."
            );
        }

        validateGroupMembership(
                groupId,
                request.getFromUserId()
        );

        validateGroupMembership(
                groupId,
                request.getToUserId()
        );

        if (request.getAmount() == null ||
                request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {

            throw new InvalidExpenseException(
                    "Settlement amount must be greater than zero."
            );
        }

        User fromUser = userRepository.findById(request.getFromUserId())
                .orElseThrow(() ->
                        new RuntimeException("From user not found")
                );

        User toUser = userRepository.findById(request.getToUserId())
                .orElseThrow(() ->
                        new RuntimeException("To user not found")
                );

        Settlement settlement = new Settlement();

        settlement.setGroup(group);
        settlement.setFromUser(fromUser);
        settlement.setToUser(toUser);
        settlement.setAmount(
                request.getAmount().setScale(2)
        );
        settlement.setNote(request.getNote());

        Settlement savedSettlement =
                settlementRepository.save(settlement);

        activityLogService.log(
                currentUser,
                group,
                ActivityType.SETTLEMENT_RECORDED,
                fromUser.getName()
                        + " paid "
                        + toUser.getName()
                        + " ₹"
                        + savedSettlement.getAmount(),
                savedSettlement.getId()
        );

        return buildResponse(savedSettlement);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SettlementRecordResponse> getSettlementHistory(
            Long groupId,
            String userEmail
    ) {

        User currentUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() ->
                        new RuntimeException("User not found")
                );

        groupRepository.findById(groupId)
                .orElseThrow(() ->
                        new RuntimeException("Group not found")
                );

        validateGroupMembership(groupId, currentUser.getId());

        return settlementRepository
                .findByGroupIdOrderBySettledAtDesc(groupId)
                .stream()
                .map(this::buildResponse)
                .toList();
    }

    private void validateGroupMembership(
            Long groupId,
            Long userId
    ) {

        boolean member =
                groupMemberRepository
                        .existsByGroupIdAndUserId(groupId, userId);

        if (!member) {
            throw new GroupAccessDeniedException(
                    "You are not a member of this group."
            );
        }
    }

    private SettlementRecordResponse buildResponse(
            Settlement settlement
    ) {

        SettlementRecordResponse response =
                new SettlementRecordResponse();

        response.setId(settlement.getId());

        response.setGroupId(
                settlement.getGroup().getId()
        );

        response.setFromUserId(
                settlement.getFromUser().getId()
        );

        response.setFromUserName(
                settlement.getFromUser().getName()
        );

        response.setToUserId(
                settlement.getToUser().getId()
        );

        response.setToUserName(
                settlement.getToUser().getName()
        );

        response.setAmount(
                settlement.getAmount()
        );

        response.setNote(
                settlement.getNote()
        );

        response.setSettledAt(
                settlement.getSettledAt()
        );

        return response;
    }
}