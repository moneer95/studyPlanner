package com.studyplanner.config;

import com.studyplanner.domain.Question;
import com.studyplanner.domain.Quiz;
import com.studyplanner.domain.UserRole;
import com.studyplanner.repository.QuizRepository;
import com.studyplanner.repository.UserRepository;
import com.studyplanner.service.AuthenticationService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner seed(UserRepository userRepository,
                           AuthenticationService authenticationService,
                           QuizRepository quizRepository) {
        return args -> {
            if (!userRepository.existsByUsername("admin")) {
                authenticationService.register("admin", "admin123", "admin@example.com", UserRole.ADMIN);
            }
            if (!userRepository.existsByUsername("tutor")) {
                authenticationService.register("tutor", "tutor123", "tutor@example.com", UserRole.TUTOR);
            }
            if (!userRepository.existsByUsername("student")) {
                authenticationService.register("student", "student123", "student@example.com", UserRole.STUDENT);
            }

            if (quizRepository.count() == 0) {
                Quiz sample = new Quiz("Sample fundamentals quiz", "Practice questions for progress tracking");
                sample = quizRepository.save(sample);
                Question q1 = new Question(
                        "What is 2 + 2?",
                        "3", "4", "5", "22",
                        1,
                        "Arithmetic",
                        sample
                );
                Question q2 = new Question(
                        "Capital of France?",
                        "Berlin", "Madrid", "Paris", "Rome",
                        2,
                        "Geography",
                        sample
                );
                sample.getQuestions().add(q1);
                sample.getQuestions().add(q2);
                quizRepository.save(sample);
            }
        };
    }
}
