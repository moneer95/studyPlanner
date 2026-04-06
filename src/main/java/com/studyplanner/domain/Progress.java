package com.studyplanner.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "progress")
public class Progress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id")
    private Student student;

    @Column(nullable = false, length = 200)
    private String topic;

    @Column(nullable = false)
    private double averageScorePercent;

    @Column(nullable = false)
    private int attemptCount;

    @Column(length = 1000)
    private String revisionSuggestion;

    protected Progress() {
    }

    public Progress(Student student, String topic, double averageScorePercent, int attemptCount, String revisionSuggestion) {
        this.student = student;
        this.topic = topic;
        this.averageScorePercent = averageScorePercent;
        this.attemptCount = attemptCount;
        this.revisionSuggestion = revisionSuggestion;
    }

    public Long getId() {
        return id;
    }

    public Student getStudent() {
        return student;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public double getAverageScorePercent() {
        return averageScorePercent;
    }

    public void setAverageScorePercent(double averageScorePercent) {
        this.averageScorePercent = averageScorePercent;
    }

    public int getAttemptCount() {
        return attemptCount;
    }

    public void setAttemptCount(int attemptCount) {
        this.attemptCount = attemptCount;
    }

    public String getRevisionSuggestion() {
        return revisionSuggestion;
    }

    public void setRevisionSuggestion(String revisionSuggestion) {
        this.revisionSuggestion = revisionSuggestion;
    }

    public boolean isWeakTopic(double thresholdPercent) {
        return averageScorePercent < thresholdPercent;
    }
}
