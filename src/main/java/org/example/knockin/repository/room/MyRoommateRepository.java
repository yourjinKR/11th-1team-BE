package org.example.knockin.repository.room;

import org.example.knockin.entity.room.MyRoommate;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MyRoommateRepository extends JpaRepository<MyRoommate, Long> {
}