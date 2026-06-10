package org.example.knockin.repository.board;

import java.util.List;

public interface RoommateBoardOptionRepositoryCustom {
    List<String> getExtraOptionsNameByBoardId(Long boardId);
}
