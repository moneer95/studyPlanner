package com.studyplanner.service.quiz;

import com.studyplanner.domain.MockExam;
import com.studyplanner.domain.Question;
import com.studyplanner.repository.QuestionRepository;
import com.studyplanner.repository.QuizRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
public class DefaultMockExamFactory implements MockExamFactory {

    private final QuizRepository quizRepository;
    private final QuestionRepository questionRepository;

    public DefaultMockExamFactory(QuizRepository quizRepository, QuestionRepository questionRepository) {
        this.quizRepository = quizRepository;
        this.questionRepository = questionRepository;
    }

    @Override
    @Transactional
    public MockExam createFromTopics(List<String> topics) {
        List<String> lowered = topics.stream()
                .map(t -> t.toLowerCase(Locale.ROOT).trim())
                .filter(t -> !t.isEmpty())
                .distinct()
                .collect(Collectors.toList());
        if (lowered.isEmpty()) {
            throw new IllegalArgumentException("Provide at least one topic");
        }
        List<Question> pool = questionRepository.findByTopicsLowercase(lowered);
        MockExam exam = new MockExam(
                "Mock exam: " + String.join(", ", topics),
                "Auto-generated from syllabus topics",
                new ArrayList<>(topics)
        );
        exam = quizRepository.save(exam);
        if (pool.isEmpty()) {
            Question placeholder = new Question(
                    "No bank questions matched these topics yet. This is a placeholder.",
                    "OK", "Skip", "Report", "N/A",
                    0,
                    topics.get(0),
                    exam
            );
            exam.getQuestions().add(placeholder);
            quizRepository.save(exam);
            return (MockExam) quizRepository.findById(exam.getId()).orElseThrow();
        }
        for (Question template : pool) {
            Question copy = new Question(
                    template.getText(),
                    template.getOptionA(),
                    template.getOptionB(),
                    template.getOptionC(),
                    template.getOptionD(),
                    template.getCorrectIndex(),
                    template.getTopic(),
                    exam
            );
            exam.getQuestions().add(copy);
        }
        return quizRepository.save(exam);
    }
}
