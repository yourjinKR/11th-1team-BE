package org.example.knockin.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.knockin.dto.*;
import org.example.knockin.dto.HouseRuleDto.Response;
import org.example.knockin.global.api.CommonResponse;
import org.example.knockin.global.auth.dto.PrincipalDetails;
import org.example.knockin.service.impl.CalendarServiceImpl;
import org.example.knockin.service.impl.HouseRuleServiceImpl;
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
    private final HouseRuleServiceImpl houseRuleService;
    private final CalendarServiceImpl calendarServiceImpl;

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

    @PostMapping("/me/house-rule")
    @Operation(summary = "하우스룰 등록")
    public CommonResponse<HouseRuleDto.Response> saveHouseRule(
            @AuthenticationPrincipal PrincipalDetails details,
            @Valid @RequestBody HouseRuleDto.Request request
    ) {
        Response response = houseRuleService.saveHouseRule(request, details.getMember().getId());
        return CommonResponse.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/me/house-rule")
    @Operation(summary = "내 하우스룰 목록 조회")
    public CommonResponse<List<HouseRuleListDto.Response>> findHouseRuleList(
            @AuthenticationPrincipal PrincipalDetails details
    ) {
        List<HouseRuleListDto.Response> responses = houseRuleService.findHouseRuleList(details.getMember().getId());
        return CommonResponse.status(HttpStatus.OK).body(responses);
    }

    @GetMapping("/me/house-rule/{id}")
    @Operation(summary = "내 하우스룰 상세 조회")
    public CommonResponse<HouseRuleDetailDto.Response> findHouseRuleDetail(
            @AuthenticationPrincipal PrincipalDetails details,
            @PathVariable Long id
    ) {
        HouseRuleDetailDto.Response response = houseRuleService.findHouseRuleDetail(details.getMember().getId(), id);
        return CommonResponse.status(HttpStatus.OK).body(response);
    }

    @PutMapping("/me/house-rule/{id}")
    @Operation(summary = "내 하우스룰 수정")
    public CommonResponse<HouseRuleDto.Response> modifyHouseRule(
            @AuthenticationPrincipal PrincipalDetails details,
            @PathVariable Long id,
            @Valid @RequestBody HouseRuleDto.Request request
    ) {
        Response response = houseRuleService.modifyHouseRule(details.getMember().getId(), id, request);
        return CommonResponse.status(HttpStatus.OK).body(response);
    }

    @DeleteMapping("/me/house-rule/{id}")
    @Operation(summary = "내 하우스룰 삭제")
    public CommonResponse<HouseRuleDto.Response> deleteHouseRuleList(
            @AuthenticationPrincipal PrincipalDetails details,
            @PathVariable Long id
    ) {
        Response response = houseRuleService.deleteHouseRule(details.getMember().getId(), id);
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
    @Operation(summary = "내 룸메이트 캘린더 일정 저장")
    public CommonResponse<CalendarDto.Response> saveMyRoomMateCalendar(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @Valid @RequestBody CalendarDto.Request request
    ) {
        CalendarDto.Response response = calendarServiceImpl.saveBasicCalendar(principalDetails.getMember().getId(), request);
        return CommonResponse.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/me/calendar/repeat")
    @Operation(summary = "내 룸메이트 캘린더 반복 일정 저장")
    public CommonResponse<RepeatCalendarDto.Response> saveMyRepeatRoomMateCalendar(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @Valid @RequestBody RepeatCalendarDto.Request request
    ) {
        RepeatCalendarDto.Response response = calendarServiceImpl.saveRepeatCalendar(principalDetails.getMember().getId(), request);
        return CommonResponse.status(HttpStatus.OK).body(response);
    }

    @PutMapping("/me/calendar/{id}")
    @Operation(summary = "내 룸메이트 캘린더 일정 수정")
    public CommonResponse<CalendarDto.Response> modifyMyRoomMateCalendar(@PathVariable Long id, @RequestBody CalendarDto.Request request) {
        return CommonResponse.status(HttpStatus.OK).body(new CalendarDto.Response());
    }

    @DeleteMapping("/me/calendar/{id}")
    @Operation(summary = "내 룸메이트 캘린더 일정 삭제")
    public CommonResponse<CalendarDto.Response> deleteMyRoomMateCalendar(@PathVariable Long id) {
        return CommonResponse.status(HttpStatus.OK).body(new CalendarDto.Response());
    }
}

