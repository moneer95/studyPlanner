package com.studyplanner.service;

import com.studyplanner.domain.Reminder;
import com.studyplanner.reminder.ReminderObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

/**
 * Simulates email/API notification delivery for reminders.
 */
@Component
public class LoggingReminderObserver implements ReminderObserver {

    private static final Logger log = LoggerFactory.getLogger(LoggingReminderObserver.class);

    private final ReminderService reminderService;

    public LoggingReminderObserver(ReminderService reminderService) {
        this.reminderService = reminderService;
    }

    @PostConstruct
    void register() {
        reminderService.addObserver(this);
    }

    @Override
    public void onReminderDispatched(Reminder reminder) {
        log.info("[Notification] to user {} : {}", reminder.getUser().getUsername(), reminder.getMessage());
    }
}
