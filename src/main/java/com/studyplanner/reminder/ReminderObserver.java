package com.studyplanner.reminder;

import com.studyplanner.domain.Reminder;

/**
 * Observer for {@link com.studyplanner.service.ReminderService} when reminders are dispatched.
 */
@FunctionalInterface
public interface ReminderObserver {

    void onReminderDispatched(Reminder reminder);
}
