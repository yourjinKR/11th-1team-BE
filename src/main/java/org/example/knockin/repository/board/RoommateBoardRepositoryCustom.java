package org.example.knockin.repository.board;

import org.example.knockin.dto.BoardDetailDto;
import org.springframework.data.domain.Page;

public interface RoommateBoardRepositoryCustom {
    Page<RoommateBoardListRow> search(RoommateBoardSearchCondition condition);
    BoardDetailDto.Response viewDetail(Long boardId);
}
