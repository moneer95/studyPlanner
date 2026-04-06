package com.studyplanner.service;

import com.studyplanner.domain.MockExam;
import com.studyplanner.domain.Progress;
import com.studyplanner.domain.Question;
import com.studyplanner.domain.Quiz;
import com.studyplanner.domain.QuizAttempt;
import com.studyplanner.domain.Student;
import com.studyplanner.repository.ProgressRepository;
import com.studyplanner.repository.QuestionRepository;
import com.studyplanner.repository.QuizAttemptRepository;
import com.studyplanner.repository.QuizRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Quiz operations including factory-style mock exam creation (Factory Method pattern in design).
 */
@Service
public class QuizService {

    private static final double WEAK_TOPIC_THRESHOLD = 60.0;

    private final QuizRepository quizRepository;
    private final QuestionRepository questionRepository;
    private final QuizAttemptRepository quizAttemptRepository;
    private final ProgressRepository progressRepository;

    public QuizService(QuizRepository quizRepository,
                       QuestionRepository questionRepository,
                       QuizAttemptRepository quizAttemptRepository,
                       ProgressRepository progressRepository) {
        this.quizRepository = quizRepository;
        this.questionRepository = questionRepository;
        this.quizAttemptRepository = quizAttemptRepository;
        this.progressRepository = progressRepository;
    }

    public List<Quiz> listQuizzes() {
        return quizRepository.findByOrderByTitleAsc();
    }

    public Quiz getQuiz(Long id) {
        return quizRepository.findByIdWithQuestions(id).orElseThrow();
    }

    /**
     * Factory Method: builds a {@link MockExam} from syllabus topics using the question bank.
     */
    @Transactional
    public MockExam createMockExam(List<String> topics) {
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
        updateProgress(student, topicResults);
        return attempt;
    }

    private void updateProgress(Student student, Map<String, List<Boolean>> topicResults) {
        for (Map.Entry<String, List<Boolean>> e : topicResults.entrySet()) {
            String topic = e.getKey();
            List<Boolean> results = e.getValue();
            long correct = results.stream().filter(b -> b).count();
            double pct = 100.0 * correct / results.size();
            var existing = progressRepository.findByStudentAndTopicIgnoreCase(student, topic);
            if (existing.isPresent()) {
                Progress p = existing.get();
                int n = p.getAttemptCount() + 1;
                double avg = (p.getAverageScorePercent() * (n - 1) + pct) / n;
                p.setAttemptCount(n);
                p.setAverageScorePercent(avg);
                p.setRevisionSuggestion(buildRevisionSuggestion(topic, avg));
                progressRepository.save(p);
            } else {
                Progress p = new Progress(student, topic, pct, 1, buildRevisionSuggestion(topic, pct));
                        progressRepository.save(p);
            }
        }
    }

    private static String buildRevisionSuggestion(String topic, double avg) {
        if (avg < WEAK_TOPIC_THRESHOLD) {
            return "Focus extra practice on " + topic + ": review notes, textbook chapter, and short quizzes.";
        }
        return "Keep reinforcing " + topic + " with spaced review.";
    }

    public List<QuizAttempt> attemptsForStudent(Student student) {
        return quizAttemptRepository.findByStudentOrderByCompletedAtDesc(student);
    }

    public List<Progress> progressForStudent(Student student) {
        return progressRepository.findByStudentOrderByAverageScorePercentAsc(student);
    }

    public List<Progress> weakTopics(Student student) {
        return progressForStudent(student).stream()
                .filter(p -> p.isWeakTopic(WEAK_TOPIC_THRESHOLD))
                .collect(Collectors.toList());
    }
}
