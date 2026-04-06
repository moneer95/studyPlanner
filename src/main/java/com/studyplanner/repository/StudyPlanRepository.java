package com.studyplanner.repository;

import com.studyplanner.domain.StudyPlan;
import com.studyplanner.domain.Student;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StudyPlanRepository extends JpaRepository<StudyPlan, Long> {

    List<StudyPlan> findByStudentOrderByTitleAsc(Student student);
}
