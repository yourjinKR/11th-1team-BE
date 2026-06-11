package org.example.knockin.repository.board;

import java.util.Optional;
import org.example.knockin.repository.board.row.BasicInfoRow;
import org.example.knockin.repository.board.row.EditFormRow;
import org.springframework.data.domain.Page;

public interface RoommateBoardRepositoryCustom {
    Page<RoommateBoardListRow> search(RoommateBoardSearchCondition condition);
    Optional<BasicInfoRow> getBasicInfo(Long boardId);

    Optional<EditFormRow> getEditRow(Long boardId);
}
