package org.example.knockin.repository.board;

import java.util.List;
import org.example.knockin.dto.BoardDetailDto.Response.FileDetailDto;

public interface RoommateBoardFileRepositoryCustom {
    List<FileDetailDto> getFileDetailDtoByBoardId(Long boardId);
}
