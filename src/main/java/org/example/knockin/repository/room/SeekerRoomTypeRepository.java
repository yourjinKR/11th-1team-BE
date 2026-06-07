package org.example.knockin.repository.room;

import org.example.knockin.entity.room.RoomSeekerProfile;
import org.example.knockin.entity.room.SeekerRoomType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SeekerRoomTypeRepository extends JpaRepository<SeekerRoomType, Long> {
    void deleteByRoomSeekerProfile(RoomSeekerProfile roomSeekerProfile);
}