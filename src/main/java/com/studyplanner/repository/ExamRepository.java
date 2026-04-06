package com.studyplanner.repository;

import com.studyplanner.domain.Exam;
import com.studyplanner.domain.Student;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExamRepository extends JpaRepository<Exam, Long> {

    List<Exam> findByStudentOrderByDeadlineAsc(Student student);
}
