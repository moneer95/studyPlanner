package com.studyplanner.repository;

import com.studyplanner.domain.Intervention;
import com.studyplanner.domain.Student;
import com.studyplanner.domain.Tutor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InterventionRepository extends JpaRepository<Intervention, Long> {

    List<Intervention> findByTutorOrderByCreatedAtDesc(Tutor tutor);

    List<Intervention> findByStudentOrderByCreatedAtDesc(Student student);
}
