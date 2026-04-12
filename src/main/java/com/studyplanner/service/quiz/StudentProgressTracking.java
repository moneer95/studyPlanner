package com.studyplanner.service.quiz;

import com.studyplanner.domain.Progress;
import com.studyplanner.domain.Student;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Tracks per-topic scores and weak areas (SRP: progress only).
 */
public interface StudentProgressTracking {

    void applyTopicResults(Student student, Map<String, List<Boolean>> topicResults);

    List<Progress> progressForStudent(Student student);

    List<Progress> weakTopics(Student student);

    Set<String> weakTopicNamesLowercase(Student student);
}
