package org.example.knockin.repository.room;

import org.example.knockin.entity.room.RoomSeekerProfile;
import org.example.knockin.entity.room.RoomSeekerProfileRegion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoomSeekerProfileRegionRepository extends JpaRepository<RoomSeekerProfileRegion, Long> {
    void deleteByRoomSeekerProfile(RoomSeekerProfile roomSeekerProfile);
}