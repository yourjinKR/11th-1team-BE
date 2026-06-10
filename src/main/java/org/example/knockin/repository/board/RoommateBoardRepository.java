package org.example.knockin.repository.board;

import org.example.knockin.entity.board.RoommateBoard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RoommateBoardRepository extends JpaRepository<RoommateBoard, Long>, RoommateBoardRepositoryCustom {
    @Modifying(clearAutomatically = true)
    @Query("UPDATE RoommateBoard r SET r.hits = r.hits + 1 WHERE r.id = :boardId AND r.isDeleted = false")
    int increaseHitsById(@Param("boardId") Long boardId);
}