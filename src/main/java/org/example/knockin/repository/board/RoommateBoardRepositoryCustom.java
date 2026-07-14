package org.example.knockin.repository.board;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.example.knockin.dto.BoBoardDetailDto;
import org.example.knockin.dto.BoBoardListDto;
import org.example.knockin.dto.BoardListDto;
import org.example.knockin.entity.member.Member;
import org.example.knockin.repository.board.row.BasicInfoRow;
import org.example.knockin.repository.board.row.BoardBaseRow;
import org.example.knockin.repository.board.row.EditFormRow;
import org.example.knockin.repository.board.row.MyRoommateBoardRow;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface RoommateBoardRepositoryCustom {
    Page<BoardBaseRow> search(BoardListDto.Request request, Pageable pageable, LocalDateTime endDate);
    Optional<BasicInfoRow> getBasicInfo(Long boardId);
    Page<MyRoommateBoardRow> findMyBoardList(Pageable page, Member member);
    Optional<EditFormRow> getEditRow(Long boardId);
    List<BoBoardListDto.Response.BoardInfo> findBackOfficeBoardList(Pageable pageable, BoBoardListDto.Request request);
    BoBoardDetailDto.Response findBackOffcieBoard(Long id);
}
