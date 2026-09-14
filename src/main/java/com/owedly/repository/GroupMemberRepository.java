package com.owedly.repository;

import com.owedly.entity.GroupMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GroupMemberRepository
        extends JpaRepository<GroupMember, Long> {

    boolean existsByGroupIdAndUserId(
            Long groupId,
            Long userId
    );

    List<GroupMember> findByGroupId(Long groupId);

    List<GroupMember> findByUserId(Long userId);

    long countByGroupId(Long groupId);
}