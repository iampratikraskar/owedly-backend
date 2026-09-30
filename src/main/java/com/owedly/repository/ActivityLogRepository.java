package com.owedly.repository;

import com.owedly.entity.ActivityLog;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ActivityLogRepository
        extends JpaRepository<ActivityLog, Long> {

    List<ActivityLog> findByUserIdOrderByCreatedAtDesc(
            Long userId,
            Pageable pageable
    );

    List<ActivityLog> findByGroupIdOrderByCreatedAtDesc(
            Long groupId,
            Pageable pageable
    );
}