package org.example.knockin.repository.room;

import org.example.knockin.entity.room.RoomExtraOption;
import org.example.knockin.repository.board.RoommateBoardOptionRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoomExtraOptionRepository extends JpaRepository<RoomExtraOption, Long>,
        RoommateBoardOptionRepositoryCustom {
}