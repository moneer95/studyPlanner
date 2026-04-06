package com.studyplanner.domain;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "mock_exams")
@PrimaryKeyJoinColumn(name = "quiz_id")
public class MockExam extends Quiz {

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "mock_exam_topics", joinColumns = @JoinColumn(name = "quiz_id"))
    @Column(name = "topic", length = 200)
    private List<String> syllabusTopics = new ArrayList<>();

    protected MockExam() {
    }

    public MockExam(String title, String description, List<String> syllabusTopics) {
        super(title, description);
        if (syllabusTopics != null) {
            this.syllabusTopics = new ArrayList<>(syllabusTopics);
        }
    }

    public List<String> getSyllabusTopics() {
        return syllabusTopics;
    }

    public void setSyllabusTopics(List<String> syllabusTopics) {
        this.syllabusTopics = syllabusTopics != null ? new ArrayList<>(syllabusTopics) : new ArrayList<>();
    }
}
