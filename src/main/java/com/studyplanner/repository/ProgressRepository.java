package com.studyplanner.repository;

import com.studyplanner.domain.Progress;
import com.studyplanner.domain.Student;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProgressRepository extends JpaRepository<Progress, Long> {

    List<Progress> findByStudentOrderByAverageScorePercentAsc(Student student);

    Optional<Progress> findByStudentAndTopicIgnoreCase(Student student, String topic);
}
