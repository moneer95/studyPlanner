package com.studyplanner.service.quiz;

import com.studyplanner.domain.Question;
import com.studyplanner.domain.Quiz;
import com.studyplanner.domain.QuizAttempt;
import com.studyplanner.domain.Student;
import com.studyplanner.repository.QuizAttemptRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DefaultQuizGrading implements QuizGrading {

    private final QuizAttemptRepository quizAttemptRepository;
    private final StudentProgressTracking studentProgressTracking;

    public DefaultQuizGrading(
            QuizAttemptRepository quizAttemptRepository,
            StudentProgressTracking studentProgressTracking
    ) {
        this.quizAttemptRepository = quizAttemptRepository;
        this.studentProgressTracking = studentProgressTracking;
    }

    @Override
    @Transactional
    public QuizAttempt submitQuiz(Student student, Quiz quiz, Map<Long, Integer> answersByQuestionId) {
        List<Question> questions = quiz.getQuestions();
        int max = questions.size();
        int score = 0;
        StringBuilder feedback = new StringBuilder();
        Map<String, List<Boolean>> topicResults = new HashMap<>();
        for (Question q : questions) {
            int chosen = answersByQuestionId.getOrDefault(q.getId(), -1);
            boolean correct = chosen == q.getCorrectIndex();
            if (correct) {
                score++;
            }
            String topic = q.getTopic() != null ? q.getTopic() : "General";
            topicResults.computeIfAbsent(topic, t -> new ArrayList<>()).add(correct);
            feedback.append(String.format("%s: %s%n", q.getText().substring(0, Math.min(60, q.getText().length())),
                    correct ? "Correct" : "Incorrect — review this concept."));
        }
        String summary = String.format("Score: %d / %d (%.0f%%).%n%n%s",
                score, max, max == 0 ? 0 : (100.0 * score / max), feedback);
        QuizAttempt attempt = new QuizAttempt(student, quiz, score, max, summary, Instant.now());
        quizAttemptRepository.save(attempt);
        studentProgressTracking.applyTopicResults(student, topicResults);
        return attempt;
    }

    @Override
    public List<QuizAttempt> attemptsForStudent(Student student) {
        return quizAttemptRepository.findByStudentOrderByCompletedAtDesc(student);
    }
}
