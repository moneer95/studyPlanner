package com.studyplanner.repository;

import com.studyplanner.domain.Student;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StudentRepository extends JpaRepository<Student, Long> {

    List<Student> findAllByOrderByUsernameAsc();
}
