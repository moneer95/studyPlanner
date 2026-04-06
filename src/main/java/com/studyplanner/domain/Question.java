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
@Table(name = "questions")
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 2000)
    private String text;

    @Column(nullable = false, length = 500)
    private String optionA;

    @Column(nullable = false, length = 500)
    private String optionB;

    @Column(nullable = false, length = 500)
    private String optionC;

    @Column(nullable = false, length = 500)
    private String optionD;

    /** 0–3 index of correct option */
    @Column(nullable = false)
    private int correctIndex;

    @Column(length = 200)
    private String topic;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "quiz_id")
    private Quiz quiz;

    protected Question() {
    }

    public Question(String text, String optionA, String optionB, String optionC, String optionD,
                    int correctIndex, String topic, Quiz quiz) {
        this.text = text;
        this.optionA = optionA;
        this.optionB = optionB;
        this.optionC = optionC;
        this.optionD = optionD;
        this.correctIndex = correctIndex;
        this.topic = topic;
        this.quiz = quiz;
    }

    public Long getId() {
        return id;
    }

    public String getText() {
        return text;
    }

    public String getOptionA() {
        return optionA;
    }

    public String getOptionB() {
        return optionB;
    }

    public String getOptionC() {
        return optionC;
    }

    public String getOptionD() {
        return optionD;
    }

    public int getCorrectIndex() {
        return correctIndex;
    }

    public String getTopic() {
        return topic;
    }

    public Quiz getQuiz() {
        return quiz;
    }

    public void setQuiz(Quiz quiz) {
        this.quiz = quiz;
    }
}
