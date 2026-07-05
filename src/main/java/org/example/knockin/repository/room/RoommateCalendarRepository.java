package org.example.knockin.repository.room;

import java.util.List;
import org.example.knockin.entity.room.MyRoommate;
import org.example.knockin.entity.room.RoommateCalendar;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoommateCalendarRepository extends JpaRepository<RoommateCalendar, Long>, RoommateCalendarRepositoryCustom {
    List<RoommateCalendar> findByMyRoommate(MyRoommate myRoommate);
}