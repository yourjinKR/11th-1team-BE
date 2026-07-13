package org.example.knockin.repository.board;

import java.util.List;
import org.example.knockin.dto.BoardDetailDto.Response.FileDetailDto;
import org.example.knockin.repository.board.row.BoardThumbnailRow;

public interface RoommateBoardFileRepositoryCustom {
    List<FileDetailDto> getFileDetailDtoByBoardId(Long boardId);

    List<BoardThumbnailRow> findThumbnailsByBoardIds(List<Long> boardIds);
}
