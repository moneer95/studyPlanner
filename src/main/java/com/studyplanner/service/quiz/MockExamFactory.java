package com.studyplanner.service.quiz;

import com.studyplanner.domain.MockExam;

import java.util.List;

/**
 * Builds mock exams from syllabus topics (SRP + OCP-friendly factory boundary).
 */
public interface MockExamFactory {

    MockExam createFromTopics(List<String> topics);
}
