package com.studyplanner.service.quiz;

import com.studyplanner.domain.Question;
import com.studyplanner.domain.Quiz;
import com.studyplanner.repository.QuestionRepository;
import com.studyplanner.repository.QuizRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DefaultQuestionBank implements QuestionBank {

    private final QuizRepository quizRepository;
    private final QuestionRepository questionRepository;

    public DefaultQuestionBank(QuizRepository quizRepository, QuestionRepository questionRepository) {
        this.quizRepository = quizRepository;
        this.questionRepository = questionRepository;
    }

    @Override
    @Transactional
    public void addQuestionToQuiz(
            long quizId,
            String text,
            String optionA,
            String optionB,
            String optionC,
            String optionD,
            int correctIndex,
            String topic
    ) {
        Quiz quiz = quizRepository.findById(quizId).orElseThrow();
        String t = topic != null && !topic.isBlank() ? topic : "General";
        Question q = new Question(text, optionA, optionB, optionC, optionD, correctIndex, t, quiz);
        quiz.getQuestions().add(q);
        quizRepository.save(quiz);
    }

    @Override
    public List<String> listDistinctQuestionTopics() {
        List<String> fromBank = questionRepository.findDistinctTopics();
        if (!fromBank.isEmpty()) {
            return fromBank;
        }
        return List.of("Arithmetic", "Geography", "General");
    }
}
