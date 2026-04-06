package com.studyplanner.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;

import java.util.ArrayList;
import java.util.List;

@Entity
@DiscriminatorValue("STUDENT")
public class Student extends User {

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<StudyPlan> studyPlans = new ArrayList<>();

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Exam> exams = new ArrayList<>();

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Progress> progressRecords = new ArrayList<>();

    public Student() {
    }

    public Student(String username, String passwordHash, String email) {
        super(username, passwordHash, email, UserRole.STUDENT);
    }

    public List<StudyPlan> getStudyPlans() {
        return studyPlans;
    }

    public List<Exam> getExams() {
        return exams;
    }

    public List<Progress> getProgressRecords() {
        return progressRecords;
    }
}
