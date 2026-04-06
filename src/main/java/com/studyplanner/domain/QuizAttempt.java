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

import java.time.Instant;

@Entity
@Table(name = "quiz_attempts")
public class QuizAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id")
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "quiz_id")
    private Quiz quiz;

    @Column(nullable = false)
    private int score;

    @Column(nullable = false)
    private int maxScore;

    @Column(length = 4000)
    private String feedbackSummary;

    @Column(nullable = false)
    private Instant completedAt;

    protected QuizAttempt() {
    }

    public QuizAttempt(Student student, Quiz quiz, int score, int maxScore, String feedbackSummary, Instant completedAt) {
        this.student = student;
        this.quiz = quiz;
        this.score = score;
        this.maxScore = maxScore;
        this.feedbackSummary = feedbackSummary;
        this.completedAt = completedAt;
    }

    public Long getId() {
        return id;
    }

    public Student getStudent() {
        return student;
    }

    public Quiz getQuiz() {
        return quiz;
    }

    public int getScore() {
        return score;
    }

    public int getMaxScore() {
        return maxScore;
    }

    public String getFeedbackSummary() {
        return feedbackSummary;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }
}
