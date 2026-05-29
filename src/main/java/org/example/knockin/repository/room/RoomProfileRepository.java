package org.example.knockin.repository.room;

import org.example.knockin.entity.room.RoomProfile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoomProfileRepository extends JpaRepository<RoomProfile, Long> {
}