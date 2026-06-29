package org.example.knockin.repository.room;

import org.example.knockin.entity.room.RoommateCalendarMember;
import org.example.knockin.entity.room.RoommateCalendarMemberId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoommateCalendarMemberRepository extends JpaRepository<RoommateCalendarMember, RoommateCalendarMemberId> {
}
