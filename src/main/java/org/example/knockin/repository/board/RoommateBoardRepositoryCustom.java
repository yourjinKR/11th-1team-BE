package org.example.knockin.repository.board;

import java.util.Optional;
import org.example.knockin.dto.BoardDetailDto;
import org.springframework.data.domain.Page;

public interface RoommateBoardRepositoryCustom {
    Page<RoommateBoardListRow> search(RoommateBoardSearchCondition condition);
    Optional<BoardDetailDto.Response.BasicInfo> getBasicInfo(Long boardId);
}
