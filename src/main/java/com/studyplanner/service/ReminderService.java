package com.studyplanner.service;

import com.studyplanner.domain.Exam;
import com.studyplanner.domain.Reminder;
import com.studyplanner.domain.ReminderType;
import com.studyplanner.domain.StudySession;
import com.studyplanner.domain.User;
import com.studyplanner.reminder.ReminderObserver;
import com.studyplanner.repository.ReminderRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Reminder scheduling and dispatch; notifies {@link ReminderObserver}s (Observer pattern).
 */
@Service
public class ReminderService {

    private final ReminderRepository reminderRepository;
    private final List<ReminderObserver> observers = new CopyOnWriteArrayList<>();

    public ReminderService(ReminderRepository reminderRepository) {
        this.reminderRepository = reminderRepository;
    }

    public void addObserver(ReminderObserver observer) {
        observers.add(observer);
    }

    @Transactional
    public Reminder scheduleExamReminder(User user, Exam exam, Instant remindAt) {
        String msg = "Reminder: exam \"" + exam.getTitle() + "\" deadline approaching.";
        Reminder r = new Reminder(user, msg, remindAt, ReminderType.EXAM);
        return reminderRepository.save(r);
    }

    @Transactional
    public Reminder scheduleStudySessionReminder(User user, StudySession session, Instant remindAt) {
        String msg = "Reminder: study session on \"" + session.getTopic() + "\".";
        Reminder r = new Reminder(user, msg, remindAt, ReminderType.STUDY_SESSION);
        return reminderRepository.save(r);
    }

    /** Default: remind 24h before exam deadline */
    @Transactional
    public Reminder scheduleDefaultExamReminder(User user, Exam exam) {
        Instant remindAt = exam.getDeadline().minus(24, ChronoUnit.HOURS);
        if (remindAt.isBefore(Instant.now())) {
            remindAt = Instant.now().plus(1, ChronoUnit.HOURS);
        }
        return scheduleExamReminder(user, exam, remindAt);
    }

    public List<Reminder> listForUser(Long userId) {
        return reminderRepository.findByUserIdOrderByRemindAtAsc(userId);
    }

    @Scheduled(fixedDelayString = "${studyplanner.reminder.poll-ms:60000}")
    @Transactional
    public void dispatchDueReminders() {
        Instant now = Instant.now();
        List<Reminder> due = reminderRepository.findDueWithUser(now.plusSeconds(1));
        for (Reminder r : due) {
            r.setSent(true);
            reminderRepository.save(r);
            for (ReminderObserver o : observers) {
                o.onReminderDispatched(r);
            }
        }
    }
}
