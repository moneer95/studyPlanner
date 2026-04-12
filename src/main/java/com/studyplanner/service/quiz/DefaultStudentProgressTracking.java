package com.studyplanner.service.quiz;

import com.studyplanner.domain.Progress;
import com.studyplanner.domain.Student;
import com.studyplanner.repository.ProgressRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class DefaultStudentProgressTracking implements StudentProgressTracking {

    private static final double WEAK_TOPIC_THRESHOLD = 60.0;

    private final ProgressRepository progressRepository;

    public DefaultStudentProgressTracking(ProgressRepository progressRepository) {
        this.progressRepository = progressRepository;
    }

    @Override
    @Transactional
    public void applyTopicResults(Student student, Map<String, List<Boolean>> topicResults) {
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

    @Override
    public List<Progress> progressForStudent(Student student) {
        return progressRepository.findByStudentOrderByAverageScorePercentAsc(student);
    }

    @Override
    public List<Progress> weakTopics(Student student) {
        return progressForStudent(student).stream()
                .filter(p -> p.isWeakTopic(WEAK_TOPIC_THRESHOLD))
                .collect(Collectors.toList());
    }

    @Override
    public Set<String> weakTopicNamesLowercase(Student student) {
        return weakTopics(student).stream()
                .map(p -> p.getTopic().toLowerCase(Locale.ROOT))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private static String buildRevisionSuggestion(String topic, double avg) {
        if (avg < WEAK_TOPIC_THRESHOLD) {
            return "Focus extra practice on " + topic + ": review notes, textbook chapter, and short quizzes.";
        }
        return "Keep reinforcing " + topic + " with spaced review.";
    }
}
