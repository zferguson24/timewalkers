package com.wow.timewalkers.repository;

import com.wow.timewalkers.entity.TimewalkingEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TimewalkingEventRepository extends JpaRepository<TimewalkingEvent, Long> {

    // Finds all upcoming events (including Turbulent Timeways) ordered chronologically.
    List<TimewalkingEvent> findByStartDateGreaterThanEqualOrderByStartDateAsc(LocalDate date);
}
