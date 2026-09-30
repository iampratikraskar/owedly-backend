package com.owedly.repository;

import com.owedly.entity.Settlement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SettlementRepository extends JpaRepository<Settlement, Long> {

    List<Settlement> findByGroupIdOrderBySettledAtDesc(Long groupId);

    List<Settlement> findByFromUserIdOrToUserIdOrderBySettledAtDesc(
            Long fromUserId,
            Long toUserId
    );
}