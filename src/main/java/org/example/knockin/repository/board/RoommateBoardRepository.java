package org.example.knockin.repository.board;

import org.example.knockin.entity.board.RoommateBoard;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoommateBoardRepository extends JpaRepository<RoommateBoard, Long> {
}