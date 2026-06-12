package org.example.knockin.service;

import java.io.IOException;
import java.util.List;
import org.example.knockin.dto.BoardDetailDto;
import org.example.knockin.dto.BoardDto;
import org.example.knockin.dto.BoardEditDto;
import org.example.knockin.dto.BoardListDto;
import org.example.knockin.dto.BoardListDto.Response;
import org.example.knockin.dto.BoardModifyDto;
import org.jspecify.annotations.Nullable;
import org.example.knockin.dto.MyBoardListDto;
import org.example.knockin.entity.member.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

public interface RoommateBoardService {
    BoardDto.Response save(BoardDto.Request request, Long memberId);
    Page<Response> getBoardList(BoardListDto.Request request, Pageable pageable);
    BoardDetailDto.Response getBoardDetail(Long boardId);
    Page<MyBoardListDto.Response.BoardItem> getMyBoardList(Pageable page, Member member);
    BoardEditDto.Response getEditForm(Long memberId, Long boardId);

    @Transactional(rollbackFor = IOException.class)
    BoardModifyDto.Response modify(Long memberId, Long boardId, BoardModifyDto.Request request, @Nullable List<MultipartFile> files);
}
