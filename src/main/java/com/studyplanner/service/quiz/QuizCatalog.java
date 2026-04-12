package com.studyplanner.service.quiz;

import com.studyplanner.domain.Quiz;

import java.util.List;

/**
 * Read-only access to quizzes in the catalog (SRP: quiz lookup / listing).
 */
public interface QuizCatalog {

    List<Quiz> listQuizzes();

    Quiz getQuiz(Long id);
}
