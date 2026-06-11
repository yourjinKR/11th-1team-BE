package org.example.knockin.repository.board;

import org.example.knockin.entity.board.RoommateBoardOption;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoommateBoardOptionRepository extends JpaRepository<RoommateBoardOption, Long>, RoommateBoardOptionRepositoryCustom {
}