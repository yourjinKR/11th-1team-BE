package org.example.knockin.repository.board;

import java.util.List;
import org.example.knockin.entity.board.RoommateBoard;
import org.example.knockin.entity.board.RoommateBoardFile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoommateBoardFileRepository extends JpaRepository<RoommateBoardFile, Long>, RoommateBoardFileRepositoryCustom {
    List<RoommateBoardFile> findByRoommateBoard(RoommateBoard roommateBoard);
}