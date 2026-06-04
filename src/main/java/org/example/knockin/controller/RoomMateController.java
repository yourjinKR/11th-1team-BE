package org.example.knockin.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.knockin.dto.*;
import org.example.knockin.dto.BoardDto.Response;
import org.example.knockin.entity.member.Gender;
import org.example.knockin.entity.member.Member;
import org.example.knockin.global.api.CommonResponse;
import org.example.knockin.global.auth.dto.PrincipalDetails;
import org.example.knockin.service.RoommateBoardService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/roommate")
@Tag(name = "4. 룸메이트 게시물/매칭")
public class RoomMateController {

    public final RoommateBoardService roommateBoardService;

    @GetMapping("/boards")
    @Operation(summary = "게시글 목록 조회")
    public CommonResponse<BoardListDto.Response> findBoardList(
            @RequestParam(required = false) Long region,
            @RequestParam(required = false) Gender gender,
            @RequestParam(required = false) Integer minDeposit,
            @RequestParam(required = false) Integer maxDeposit,
            @RequestParam(required = false) Integer minMounthRent,
            @RequestParam(required = false) Integer maxMounthRent,
            @RequestParam(required = false) Integer type,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return CommonResponse.status(HttpStatus.OK).body(new BoardListDto.Response());
    }

    @GetMapping("/boards/{boardId}")
    @Operation(summary = "게시글 상세 조회")
    public CommonResponse<BoardDetailDto.Response> findBoard(@PathVariable Long boardId) {
        return CommonResponse.status(HttpStatus.OK).body(new BoardDetailDto.Response());
    }

    @PostMapping("/boards/likes")
    @Operation(summary = "게시글 찜하기")
    public CommonResponse<BoardDto.Response> likeBoard(@RequestBody Map<String, Long> request) {
        return CommonResponse.status(HttpStatus.OK).body(new BoardDto.Response());
    }

    @PostMapping("/boards")
    @Operation(summary = "게시글 저장")
    public CommonResponse<BoardDto.Response> saveBoard(
            @RequestBody BoardDto.Request request,
            @AuthenticationPrincipal PrincipalDetails details) {
        Long memberId = details.getMember().getId();
        Response response = roommateBoardService.save(request, memberId);
        return CommonResponse.status(HttpStatus.OK).body(response);
    }

    @PutMapping("/boards/{boardId}")
    @Operation(summary = "게시글 수정")
    public CommonResponse<BoardDto.Response> modifyBoard(@PathVariable Long boardId, @RequestBody BoardDto.Request request) {
        return CommonResponse.status(HttpStatus.OK).body(new BoardDto.Response());
    }

    @PostMapping("/boards/{boardId}/reports")
    @Operation(summary = "게시글 신고")
    public CommonResponse<ReportDto.Response> reportBoard(@PathVariable Long boardId, @RequestBody ReportDto.Request request) {
        return CommonResponse.status(HttpStatus.OK).body(new ReportDto.Response());
    }

    @DeleteMapping("/boards/{boardId}")
    @Operation(summary = "게시글 삭제")
    public CommonResponse<BoardDto.Response> deleteBoard(@PathVariable Long boardId) {
        return CommonResponse.status(HttpStatus.OK).body(new BoardDto.Response());
    }

    @GetMapping("/matches")
    @Operation(summary = "매칭 목록 조회")
    public CommonResponse<MatchListDto.Response> findMatchList() {
        return CommonResponse.status(HttpStatus.OK).body(new MatchListDto.Response());
    }

    @GetMapping("/matches/{userId}")
    @Operation(summary = "매칭 상세 조회")
    public CommonResponse<MatchDetailDto.Response> findMatch(@PathVariable Long userId) {
        return CommonResponse.status(HttpStatus.OK).body(new MatchDetailDto.Response());
    }

    @GetMapping("/matches/score")
    @Operation(summary = "매칭 점수 조회")
    public CommonResponse<MatchScoreDto.Response> findMatchScore() {
        return CommonResponse.status(HttpStatus.OK).body(new MatchScoreDto.Response());
    }
}

