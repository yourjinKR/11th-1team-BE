package org.example.knockin.service;

import org.example.knockin.dto.BoardDto;
import org.example.knockin.dto.BoardListDto;
import org.springframework.data.domain.Pageable;

public interface RoommateBoardService {
    BoardDto.Response save(BoardDto.Request request, Long memberId);
}
