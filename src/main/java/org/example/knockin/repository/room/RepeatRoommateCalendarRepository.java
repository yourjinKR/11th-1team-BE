package org.example.knockin.repository.room;

import java.util.Optional;
import org.example.knockin.entity.room.RepeatRoommateCalendar;
import org.example.knockin.entity.room.RoommateCalendar;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RepeatRoommateCalendarRepository extends JpaRepository<RepeatRoommateCalendar, Long> {
    Optional<RepeatRoommateCalendar> findOneByRoommateCalendar(RoommateCalendar roommateCalendar);
}
