package com.studyplanner.service.quiz;

import com.studyplanner.domain.Quiz;
import com.studyplanner.domain.QuizAttempt;
import com.studyplanner.domain.Student;

import java.util.List;
import java.util.Map;

/**
 * Records attempts and scores quizzes (SRP: grading + attempt persistence).
 */
public interface QuizGrading {

    QuizAttempt submitQuiz(Student student, Quiz quiz, Map<Long, Integer> answersByQuestionId);

    List<QuizAttempt> attemptsForStudent(Student student);
}
