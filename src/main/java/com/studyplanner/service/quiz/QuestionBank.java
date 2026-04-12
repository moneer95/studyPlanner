package com.studyplanner.service.quiz;

import java.util.List;

/**
 * Question bank maintenance and topic discovery (SRP: questions + bank metadata).
 */
public interface QuestionBank {

    void addQuestionToQuiz(
            long quizId,
            String text,
            String optionA,
            String optionB,
            String optionC,
            String optionD,
            int correctIndex,
            String topic
    );

    List<String> listDistinctQuestionTopics();
}
