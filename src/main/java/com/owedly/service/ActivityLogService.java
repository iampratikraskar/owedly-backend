package com.owedly.service;

import com.owedly.entity.ActivityType;
import com.owedly.entity.Group;
import com.owedly.entity.User;

public interface ActivityLogService {

	void log(User user, Group group, ActivityType activityType, String message, Long referenceId);
}