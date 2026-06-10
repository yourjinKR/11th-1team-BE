package org.example.knockin.repository.board;

import org.example.knockin.entity.board.RoommateBoardFile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoommateBoardFileRepository extends JpaRepository<RoommateBoardFile, Long>, RoommateBoardFileRepositoryCustom {
}