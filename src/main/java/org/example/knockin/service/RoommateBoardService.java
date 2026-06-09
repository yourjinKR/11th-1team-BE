package org.example.knockin.service;

import org.example.knockin.dto.BoardDetailDto;
import org.example.knockin.dto.BoardDto;
import org.example.knockin.dto.BoardListDto;
import org.example.knockin.dto.BoardListDto.Response;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface RoommateBoardService {
    BoardDto.Response save(BoardDto.Request request, Long memberId);
    Page<Response> getBoardList(BoardListDto.Request request, Pageable pageable);
    BoardDetailDto.Response getBoardDetail(Long boardId);
}
