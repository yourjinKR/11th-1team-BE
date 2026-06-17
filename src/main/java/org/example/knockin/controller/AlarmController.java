package org.example.knockin.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.knockin.dto.AlarmListDto;
import org.example.knockin.dto.AlarmSettingDto;
import org.example.knockin.global.api.CommonResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/alarms")
@Tag(name = "9. 알림/고객센터")
public class AlarmController {
    @GetMapping("")
    @Operation(summary = "알림 목록 조회")
    public CommonResponse<AlarmListDto.Response> findAlarmList(@PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return CommonResponse.status(HttpStatus.OK).body(new AlarmListDto.Response());
    }

    @PatchMapping("/{id}/read")
    @Operation(summary = "알림 읽음 처리")
    public CommonResponse<AlarmSettingDto.Response> modifyAlarmRead(@PathVariable Long id) {
        return CommonResponse.status(HttpStatus.OK).body(AlarmSettingDto.Response.builder().build());
    }

    @PatchMapping("/read-all")
    @Operation(summary = "알림 전체 읽음 처리")
    public CommonResponse<AlarmSettingDto.Response> modifyAllAlarmRead() {
        return CommonResponse.status(HttpStatus.OK).body(AlarmSettingDto.Response.builder().build());
    }
}

