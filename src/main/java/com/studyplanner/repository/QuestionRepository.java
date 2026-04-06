package com.studyplanner.repository;

import com.studyplanner.domain.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Long> {

    @Query("SELECT q FROM Question q WHERE LOWER(q.topic) IN :topics")
    List<Question> findByTopicsLowercase(@Param("topics") List<String> topics);
}
