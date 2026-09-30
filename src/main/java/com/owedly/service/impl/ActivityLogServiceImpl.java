package com.owedly.service.impl;

import com.owedly.entity.ActivityLog;
import com.owedly.entity.ActivityType;
import com.owedly.entity.Group;
import com.owedly.entity.User;
import com.owedly.repository.ActivityLogRepository;
import com.owedly.service.ActivityLogService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ActivityLogServiceImpl implements ActivityLogService {

    private final ActivityLogRepository activityLogRepository;

    public ActivityLogServiceImpl(
            ActivityLogRepository activityLogRepository
    ) {
        this.activityLogRepository = activityLogRepository;
    }

    @Override
    @Transactional
    public void log(
            User user,
            Group group,
            ActivityType activityType,
            String message,
            Long referenceId
    ) {

        ActivityLog activityLog = new ActivityLog();

        activityLog.setUser(user);
        activityLog.setGroup(group);
        activityLog.setActivityType(activityType);
        activityLog.setMessage(message);
        activityLog.setReferenceId(referenceId);

        activityLogRepository.save(activityLog);
    }
}