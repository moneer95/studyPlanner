package com.studyplanner.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "reminders")
public class Reminder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false, length = 500)
    private String message;

    @Column(nullable = false)
    private Instant remindAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private ReminderType reminderType;

    @Column(nullable = false)
    private boolean sent;

    protected Reminder() {
    }

    public Reminder(User user, String message, Instant remindAt, ReminderType reminderType) {
        this.user = user;
        this.message = message;
        this.remindAt = remindAt;
        this.reminderType = reminderType;
        this.sent = false;
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public String getMessage() {
        return message;
    }

    public Instant getRemindAt() {
        return remindAt;
    }

    public ReminderType getReminderType() {
        return reminderType;
    }

    public boolean isSent() {
        return sent;
    }

    public void setSent(boolean sent) {
        this.sent = sent;
    }
}
