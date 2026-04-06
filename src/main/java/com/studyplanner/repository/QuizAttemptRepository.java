package com.studyplanner.repository;

import com.studyplanner.domain.Quiz;
import com.studyplanner.domain.QuizAttempt;
import com.studyplanner.domain.Student;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Long> {

    List<QuizAttempt> findByStudentOrderByCompletedAtDesc(Student student);

    List<QuizAttempt> findByStudentAndQuizOrderByCompletedAtDesc(Student student, Quiz quiz);

    List<QuizAttempt> findByStudent(Student student);
}
