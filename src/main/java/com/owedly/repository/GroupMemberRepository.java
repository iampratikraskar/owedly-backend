package com.owedly.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.owedly.entity.GroupMember;

public interface GroupMemberRepository
        extends JpaRepository<GroupMember, Long> {

    boolean existsByGroupIdAndUserId(
            Long groupId,
            Long userId
    );

    List<GroupMember> findByGroupId(Long groupId);

    List<GroupMember> findByUserId(Long userId);

    long countByGroupId(Long groupId);
    
    long countByUserId(Long userId);
}