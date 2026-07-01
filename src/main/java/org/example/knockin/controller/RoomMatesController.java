package org.example.knockin.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.knockin.dto.CalendarDto;
import org.example.knockin.dto.CalendarTypesDto;
import org.example.knockin.dto.MyRoommateCalendarDetailDto;
import org.example.knockin.dto.MyRoommateCalendarListDto;
import org.example.knockin.dto.MyRoommateCardDto;
import org.example.knockin.dto.MyRoommateCardDto.Response;
import org.example.knockin.dto.MyRoommateDto;
import org.example.knockin.global.api.CommonResponse;
import org.example.knockin.global.auth.dto.PrincipalDetails;
import org.example.knockin.service.impl.MyRoomMateServiceImpl;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/roommates")
@Tag(name = "8. 룸메이트 관리")
public class RoomMatesController {
    private final MyRoomMateServiceImpl myRoomMateService;

    @GetMapping("/me")
    @Operation(summary = "내 룸메이트 조회")
    public CommonResponse<MyRoommateCardDto.Response> findMyRoomMate(@AuthenticationPrincipal PrincipalDetails details) {
        MyRoommateCardDto.Response response = myRoomMateService.findMyRoommate(details.getMember().getId());
        return CommonResponse.status(HttpStatus.OK).body(response);
    }

    @DeleteMapping("/me/{id}")
    @Operation(summary = "내 룸메이트 삭제")
    public CommonResponse<MyRoommateDto.Response> deleteMyRoomMate(
            @AuthenticationPrincipal PrincipalDetails details,
            @PathVariable Long id
    ) {
        MyRoommateDto.Response response = myRoomMateService.deleteMyRoommate(id, details.getMember().getId());
        return CommonResponse.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/me/calendar")
    @Operation(summary = "내 룸메이트 캘린더 목록 조회")
    public CommonResponse<MyRoommateCalendarListDto.Response> findMyRoomMateCalendarList(@RequestParam(required = false) Integer year, @RequestParam(required = false) Integer month) {
        return CommonResponse.status(HttpStatus.OK).body(new MyRoommateCalendarListDto.Response());
    }

    @GetMapping("/me/calendar/{id}")
    @Operation(summary = "내 룸메이트 캘린더 상세 조회")
    public CommonResponse<MyRoommateCalendarDetailDto.Response> findMyRoomMateCalendar(@PathVariable Long id) {
        return CommonResponse.status(HttpStatus.OK).body(new MyRoommateCalendarDetailDto.Response());
    }

    @GetMapping("/me/calendar/types")
    @Operation(summary = "캘린더 타입 조회")
    public CommonResponse<CalendarTypesDto.Response> findCalendarType() {
        return CommonResponse.status(HttpStatus.OK).body(new CalendarTypesDto.Response());
    }

    @PostMapping("/me/calendar")
    @Operation(summary = "내 룸메이트 캘린더 저장")
    public CommonResponse<CalendarDto.Response> saveMyRoomMateCalendar(@RequestBody CalendarDto.Request request) {
        return CommonResponse.status(HttpStatus.OK).body(new CalendarDto.Response());
    }

    @PutMapping("/me/calendar/{id}")
    @Operation(summary = "내 룸메이트 캘린더 수정")
    public CommonResponse<CalendarDto.Response> modifyMyRoomMateCalendar(@PathVariable Long id, @RequestBody CalendarDto.Request request) {
        return CommonResponse.status(HttpStatus.OK).body(new CalendarDto.Response());
    }

    @DeleteMapping("/me/calendar/{id}")
    @Operation(summary = "내 룸메이트 캘린더 삭제")
    public CommonResponse<CalendarDto.Response> deleteMyRoomMateCalendar(@PathVariable Long id) {
        return CommonResponse.status(HttpStatus.OK).body(new CalendarDto.Response());
    }
}

