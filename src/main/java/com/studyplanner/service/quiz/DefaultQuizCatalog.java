package com.studyplanner.service.quiz;

import com.studyplanner.domain.Quiz;
import com.studyplanner.repository.QuizRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DefaultQuizCatalog implements QuizCatalog {

    private final QuizRepository quizRepository;

    public DefaultQuizCatalog(QuizRepository quizRepository) {
        this.quizRepository = quizRepository;
    }

    @Override
    public List<Quiz> listQuizzes() {
        return quizRepository.findByOrderByTitleAsc();
    }

    @Override
    public Quiz getQuiz(Long id) {
        return quizRepository.findByIdWithQuestions(id).orElseThrow();
    }
}
