package org.example.knockin.repository.room;

import java.util.List;
import org.example.knockin.entity.room.RoommateCalendar;
import org.example.knockin.entity.room.RoommateCalendarMember;
import org.example.knockin.entity.room.RoommateCalendarMemberId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoommateCalendarMemberRepository extends JpaRepository<RoommateCalendarMember, RoommateCalendarMemberId> {
    List<RoommateCalendarMember> findByRoommateCalendar(RoommateCalendar roommateCalendar);
}
