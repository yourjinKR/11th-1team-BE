package org.example.knockin.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.knockin.dto.*;
import org.example.knockin.global.api.CommonResponse;
import org.example.knockin.global.auth.dto.PrincipalDetails;
import org.example.knockin.service.impl.AppVersionServiceImpl;
import org.example.knockin.service.impl.FaqServiceImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/bo")
@Tag(name = "10. 백오피스 [BO]")
public class BackOfficeController {
    private final FaqServiceImpl faqService;
    private final AppVersionServiceImpl appVersionService;

    @PostMapping("/terms")
    @Operation(summary = "약관 저장")
    public CommonResponse<BoTermsDto.Response> saveTerms(@RequestBody BoTermsDto.Request request) {
        return CommonResponse.status(HttpStatus.OK).body(new BoTermsDto.Response());
    }

    @PutMapping("/terms/{termsId}/draft")
    @Operation(summary = "약관 수정 (임시저장)")
    public CommonResponse<BoTermsDto.Response> modifyTerms(@PathVariable Long termsId, @RequestBody BoTermsDto.Request request) {
        return CommonResponse.status(HttpStatus.OK).body(new BoTermsDto.Response());
    }

    @GetMapping("/terms")
    @Operation(summary = "약관 목록 조회")
    public CommonResponse<BoTermsListDto.Response> findTermsList(@PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return CommonResponse.status(HttpStatus.OK).body(new BoTermsListDto.Response());
    }

    @GetMapping("/terms/{termsId}")
    @Operation(summary = "약관 상세 조회")
    public CommonResponse<BoTermsDetailDto.Response> findTerms(@PathVariable Long termsId) {
        return CommonResponse.status(HttpStatus.OK).body(new BoTermsDetailDto.Response());
    }

    @DeleteMapping("/terms/{termsId}")
    @Operation(summary = "약관 삭제")
    public CommonResponse<BoTermsDto.Response> deleteTerms(@PathVariable Long termsId) {
        return CommonResponse.status(HttpStatus.OK).body(new BoTermsDto.Response());
    }

    @PutMapping("/terms/{termsId}/publish")
    @Operation(summary = "약관 수정 (게시)")
    public CommonResponse<BoTermsDto.Response> modifyLastTerms(@PathVariable Long termsId, @RequestBody BoTermsDto.Request request) {
        return CommonResponse.status(HttpStatus.OK).body(new BoTermsDto.Response());
    }

    @PostMapping("/room-types")
    @Operation(summary = "방 유형 저장")
    public CommonResponse<BoRoomTypeDto.Response> saveRoomType(@RequestBody BoRoomTypeDto.Request request) {
        return CommonResponse.status(HttpStatus.OK).body(new BoRoomTypeDto.Response());
    }

    @GetMapping("/room-types")
    @Operation(summary = "방 유형 목록 조회")
    public CommonResponse<BoRoomTypeListDto.Response> findRoomTypeList(@PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return CommonResponse.status(HttpStatus.OK).body(new BoRoomTypeListDto.Response());
    }

    @PutMapping("/room-types/{id}")
    @Operation(summary = "방 유형 수정")
    public CommonResponse<BoRoomTypeDto.Response> modifyRoomType(@PathVariable Long id, @RequestBody BoRoomTypeDto.Request request) {
        return CommonResponse.status(HttpStatus.OK).body(new BoRoomTypeDto.Response());
    }

    @DeleteMapping("/room-types/{id}")
    @Operation(summary = "방 유형 삭제")
    public CommonResponse<BoRoomTypeDto.Response> deleteRoomType(@PathVariable Long id) {
        return CommonResponse.status(HttpStatus.OK).body(new BoRoomTypeDto.Response());
    }

    @GetMapping("/room-types/{id}")
    @Operation(summary = "방 유형 상세 조회")
    public CommonResponse<BoRoomTypeDetailDto.Response> findRoomType(@PathVariable Long id) {
        return CommonResponse.status(HttpStatus.OK).body(new BoRoomTypeDetailDto.Response());
    }

    @GetMapping("/lifestyle-patterns")
    @Operation(summary = "라이프스타일 패턴 목록 조회")
    public CommonResponse<BoLifeStylePatternListDto.Response> findLifeStylePatternList(@PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return CommonResponse.status(HttpStatus.OK).body(new BoLifeStylePatternListDto.Response());
    }

    @GetMapping("/lifestyle-patterns/{id}")
    @Operation(summary = "라이프스타일 패턴 상세 조회")
    public CommonResponse<BoLifeStylePatternDetailDto.Response> findLifeStylePattern(@PathVariable Long id) {
        return CommonResponse.status(HttpStatus.OK).body(new BoLifeStylePatternDetailDto.Response());
    }

    @PostMapping("/lifestyle-patterns")
    @Operation(summary = "라이프스타일 패턴 저장")
    public CommonResponse<BoLifeStylePatternDto.Response> saveLifeStylePattern(@RequestBody BoLifeStylePatternDto.Request request) {
        return CommonResponse.status(HttpStatus.OK).body(new BoLifeStylePatternDto.Response());
    }

    @PutMapping("/lifestyle-patterns/{id}")
    @Operation(summary = "라이프스타일 패턴 수정")
    public CommonResponse<BoLifeStylePatternDto.Response> modifyLifeStylePattern(@PathVariable Long id, @RequestBody BoLifeStylePatternDto.Request request) {
        return CommonResponse.status(HttpStatus.OK).body(new BoLifeStylePatternDto.Response());
    }

    @DeleteMapping("/lifestyle-patterns/{id}")
    @Operation(summary = "라이프스타일 패턴 삭제")
    public CommonResponse<BoLifeStylePatternDto.Response> deleteLifeStylePattern(@PathVariable Long id) {
        return CommonResponse.status(HttpStatus.OK).body(new BoLifeStylePatternDto.Response());
    }

    @GetMapping("/verifications/company")
    @Operation(summary = "업체 인증 목록 조회")
    public CommonResponse<BoVerificationCompanyListDto.Response> findVerificationsCompanyList(@PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return CommonResponse.status(HttpStatus.OK).body(new BoVerificationCompanyListDto.Response());
    }

    @PatchMapping("/verifications/company/{id}/approve")
    @Operation(summary = "업체 인증 승인")
    public CommonResponse<BoVerificationCompanyDto.Response> saveVerificationsCompany(@PathVariable Long id) {
        return CommonResponse.status(HttpStatus.OK).body(new BoVerificationCompanyDto.Response());
    }

    @PostMapping("/notices")
    @Operation(summary = "공지사항 저장")
    public CommonResponse<BoNoticeDto.Response> saveNotice(@RequestBody BoNoticeDto.Request request) {
        return CommonResponse.status(HttpStatus.OK).body(new BoNoticeDto.Response());
    }

    @GetMapping("/notices")
    @Operation(summary = "공지사항 목록 조회")
    public CommonResponse<BoNoticeListDto.Response> findNoticeList(@PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return CommonResponse.status(HttpStatus.OK).body(new BoNoticeListDto.Response());
    }

    @GetMapping("/notices/{id}")
    @Operation(summary = "공지사항 상세 조회")
    public CommonResponse<BoNoticeDetailDto.Response> findNotice(@PathVariable Long id) {
        return CommonResponse.status(HttpStatus.OK).body(new BoNoticeDetailDto.Response());
    }

    @PutMapping("/notices/{id}")
    @Operation(summary = "공지사항 수정")
    public CommonResponse<BoNoticeDto.Response> modifyNotice(@PathVariable Long id, @RequestBody BoNoticeDto.Request request) {
        return CommonResponse.status(HttpStatus.OK).body(new BoNoticeDto.Response());
    }

    @DeleteMapping("/notices/{id}")
    @Operation(summary = "공지사항 삭제")
    public CommonResponse<BoNoticeDto.Response> deleteNotice(@PathVariable Long id) {
        return CommonResponse.status(HttpStatus.OK).body(new BoNoticeDto.Response());
    }

    @PostMapping("/inquiries")
    @Operation(summary = "문의 답변 저장")
    public CommonResponse<BoInquiryReplyDto.Response> saveInquiryReply(@RequestBody BoInquiryReplyDto.Request request) {
        return CommonResponse.status(HttpStatus.OK).body(new BoInquiryReplyDto.Response());
    }

    @GetMapping("/inquiries")
    @Operation(summary = "문의 목록 조회")
    public CommonResponse<BoInquiryListDto.Response> findInquirieList(@PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return CommonResponse.status(HttpStatus.OK).body(new BoInquiryListDto.Response());
    }

    @GetMapping("/inquiries/{id}")
    @Operation(summary = "문의 상세 조회")
    public CommonResponse<BoInquiryDetailDto.Response> findInquirie(@PathVariable Long id) {
        return CommonResponse.status(HttpStatus.OK).body(new BoInquiryDetailDto.Response());
    }

    @PostMapping("/faq")
    @Operation(summary = "자주묻는 질문 저장")
    public CommonResponse<FaqSaveDto.Response> saveFaq(@RequestBody FaqSaveDto.Request request, @AuthenticationPrincipal PrincipalDetails principalDetails) {
        return CommonResponse.status(HttpStatus.OK).body(faqService.saveFaq(request, principalDetails.getMember().getId()));
    }

    @PutMapping("/faq")
    @Operation(summary = "자주묻는 질문 수정")
    public CommonResponse<FaqModifyDto.Response> modifyFaq(@RequestBody FaqModifyDto.Request request, @AuthenticationPrincipal PrincipalDetails principalDetails) {
        return CommonResponse.status(HttpStatus.OK).body(faqService.modifyFaq(request, principalDetails.getMember().getId()));
    }

    @DeleteMapping("/faq/{id}")
    @Operation(summary = "자주묻는 질문 삭제")
    public CommonResponse<FaqDeleteDto.Response> deleteFaq(@PathVariable Long id, @AuthenticationPrincipal PrincipalDetails principalDetails) {
        return CommonResponse.status(HttpStatus.OK).body(faqService.deleteFaq(id, principalDetails.getMember().getId()));
    }

    @GetMapping("/faq")
    @Operation(summary = "자주묻는 질문 목록 조회")
    public CommonResponse<FaqListDto.Response> findFaqList(@PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return CommonResponse.status(HttpStatus.OK).body(faqService.findFaqList(pageable));
    }

    @GetMapping("/faq/{id}")
    @Operation(summary = "자주묻는 질문 상세 조회")
    public CommonResponse<FaqDto.Response> findFaq(@PathVariable Long id) {
        return CommonResponse.status(HttpStatus.OK).body(faqService.findFaq(id));
    }

    @GetMapping("/app-version")
    @Operation(summary = "현재 앱버전 조회")
    public CommonResponse<AppVersionDto.Response> findAppVersion() {
        return CommonResponse.status(HttpStatus.OK).body(appVersionService.findAppVersion());
    }

    @PostMapping("/app-version")
    @Operation(summary = "앱버전 삽입")
    public CommonResponse<AppVersionSaveDto.Response> saveAppVersion(@RequestBody AppVersionSaveDto.Request request) {
        return CommonResponse.status(HttpStatus.OK).body(appVersionService.saveAppVersion(request));
    }

    @PutMapping("/app-version")
    @Operation(summary = "앱버전 수정")
    public CommonResponse<AppVersionModifyDto.Response> modifyAppVersion(@RequestBody AppVersionModifyDto.Request request) {
        return CommonResponse.status(HttpStatus.OK).body(appVersionService.modifyAppVersion(request));
    }
}

