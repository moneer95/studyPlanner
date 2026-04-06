package com.studyplanner.repository;

import com.studyplanner.domain.Reminder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface ReminderRepository extends JpaRepository<Reminder, Long> {

    @Query("SELECT r FROM Reminder r JOIN FETCH r.user WHERE r.sent = false AND r.remindAt <= :before")
    List<Reminder> findDueWithUser(@Param("before") Instant before);

    List<Reminder> findByUserIdOrderByRemindAtAsc(Long userId);
}
