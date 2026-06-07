package org.example.knockin.repository.room;

import org.example.knockin.entity.member.Member;
import org.example.knockin.entity.room.RoomProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoomProfileRepository extends JpaRepository<RoomProfile, Long> {
    List<RoomProfile> findByMember(Member member);
}