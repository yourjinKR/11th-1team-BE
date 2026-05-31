package org.example.knockin.controller;

import org.example.knockin.dto.*;
import org.example.knockin.entity.member.Gender;
import org.example.knockin.global.api.CommonResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/roommate")
public class RoomMateController {
    @GetMapping("/boards")
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
    public CommonResponse<BoardDetailDto.Response> findBoard(@PathVariable Long boardId) {
        return CommonResponse.status(HttpStatus.OK).body(new BoardDetailDto.Response());
    }

    @PostMapping("/boards/likes")
    public CommonResponse<BoardDto.Response> likeBoard(@RequestBody Map<String, Long> request) {
        return CommonResponse.status(HttpStatus.OK).body(new BoardDto.Response());
    }

    @PostMapping("/boards")
    public CommonResponse<BoardDto.Response> saveBoard(@RequestBody BoardDto.Request request) {
        return CommonResponse.status(HttpStatus.OK).body(new BoardDto.Response());
    }

    @PutMapping("/boards/{boardId}")
    public CommonResponse<BoardDto.Response> modifyBoard(@PathVariable Long boardId, @RequestBody BoardDto.Request request) {
        return CommonResponse.status(HttpStatus.OK).body(new BoardDto.Response());
    }

    @PostMapping("/boards/{boardId}/reports")
    public CommonResponse<ReportDto.Response> reportBoard(@PathVariable Long boardId, @RequestBody ReportDto.Request request) {
        return CommonResponse.status(HttpStatus.OK).body(new ReportDto.Response());
    }

    @DeleteMapping("/boards/{boardId}")
    public CommonResponse<BoardDto.Response> deleteBoard(@PathVariable Long boardId) {
        return CommonResponse.status(HttpStatus.OK).body(new BoardDto.Response());
    }

    @GetMapping("/matches")
    public CommonResponse<MatchListDto.Response> findMatchList() {
        return CommonResponse.status(HttpStatus.OK).body(new MatchListDto.Response());
    }

    @GetMapping("/matches/{userId}")
    public CommonResponse<MatchDetailDto.Response> findMatch(@PathVariable Long userId) {
        return CommonResponse.status(HttpStatus.OK).body(new MatchDetailDto.Response());
    }

    @GetMapping("/matches/score")
    public CommonResponse<MatchScoreDto.Response> findMatchScore() {
        return CommonResponse.status(HttpStatus.OK).body(new MatchScoreDto.Response());
    }
}
