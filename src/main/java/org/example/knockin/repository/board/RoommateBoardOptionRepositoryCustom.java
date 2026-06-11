package org.example.knockin.repository.board;

import java.util.List;
import org.example.knockin.dto.BoardEditDto;

public interface RoommateBoardOptionRepositoryCustom {
    List<String> getExtraOptionsNameByBoardId(Long boardId);

    List<BoardEditDto.Response.BoardOptionInfo> getExtraOptionsByBoardId(Long boardId);
}
