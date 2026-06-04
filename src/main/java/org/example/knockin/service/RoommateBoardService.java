package org.example.knockin.service;

import org.example.knockin.dto.BoardDto;
import org.example.knockin.entity.member.Member;

public interface RoommateBoardService {
    BoardDto.Response save(BoardDto.Request request, Member member);
}
