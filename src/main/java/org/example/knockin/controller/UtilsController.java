package org.example.knockin.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.knockin.dto.MetaLifestylePatternsDto;
import org.example.knockin.dto.MetaRegionsDto;
import org.example.knockin.dto.MetaRoomAddOptionsDto;
import org.example.knockin.dto.MetaRoomTypesDto;
import org.example.knockin.dto.PopularSearchDto;
import org.example.knockin.dto.TermsDetailDto;
import org.example.knockin.dto.TermsListDto;
import org.example.knockin.global.api.CommonResponse;
import org.example.knockin.service.impl.MetaServiceImpl;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Tag(name = "3. 일반/메타데이터")
public class UtilsController {
    private final MetaServiceImpl metaService;

    @GetMapping("/terms")
    @Operation(summary = "약관 목록 조회")
    public CommonResponse<TermsListDto.Response> findTermList() {
        return CommonResponse.status(HttpStatus.OK).body(metaService.findTermList());
    }

    @GetMapping("/terms/{termsId}")
    @Operation(summary = "약관 상세 조회")
    public CommonResponse<TermsDetailDto.Response> findTerm(@PathVariable Long termsId) {
        return CommonResponse.status(HttpStatus.OK).body(metaService.findTermDetail(termsId));
    }

    @GetMapping("/search/popular")
    @Operation(summary = "인기 검색어 조회")
    public CommonResponse<PopularSearchDto.Response> findPopSearch() {
        return CommonResponse.status(HttpStatus.OK).body(metaService.findPopSearch());
    }

    @GetMapping("/meta/lifestyle-patterns")
    @Operation(summary = "라이프스타일 패턴 메타데이터 조회")
    public CommonResponse<MetaLifestylePatternsDto.Response> findLifeStylePatterns() {
        return CommonResponse.status(HttpStatus.OK).body(metaService.findLifeStylePatterns());
    }

    @GetMapping("/meta/room-types")
    @Operation(summary = "방 유형 메타데이터 조회")
    public CommonResponse<MetaRoomTypesDto.Response> findRoomTypes() {
        return CommonResponse.status(HttpStatus.OK).body(metaService.findRoomTypes());
    }

    @GetMapping("/meta/regions")
    @Operation(summary = "지역 메타데이터 조회")
    public CommonResponse<MetaRegionsDto.Response> findRegions() {
        return CommonResponse.status(HttpStatus.OK).body(metaService.findRegions());
    }

    @GetMapping("/meta/room-add-options")
    @Operation(summary = "방 추가 옵션 메타데이터 조회")
    public CommonResponse<MetaRoomAddOptionsDto.Response> findRoomAddOptions() {
        return CommonResponse.status(HttpStatus.OK).body(metaService.findRoomAddOptions());
    }

    @GetMapping("/auth/success")
    public CommonResponse<String> authSuccess() {
        return CommonResponse.status(HttpStatus.OK).body("로그인에 성공하였습니다.");
    }
}

