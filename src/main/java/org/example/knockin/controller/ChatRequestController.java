package org.example.knockin.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.knockin.dto.ChatRequestDetailDto;
import org.example.knockin.dto.ChatRequestDto;
import org.example.knockin.dto.ChatRequestDto.Response;
import org.example.knockin.dto.ChatRequestListDto;
import org.example.knockin.global.api.CommonResponse;
import org.example.knockin.global.auth.dto.PrincipalDetails;
import org.example.knockin.service.impl.ChatRequestServiceImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/chat-requests")
@Tag(name = "5. 채팅 요청")
public class ChatRequestController {
    private final ChatRequestServiceImpl chatRequestService;

    @GetMapping("")
    @Operation(summary = "채팅 요청 목록 조회")
    public CommonResponse<ChatRequestListDto.Response> findChatRequestList(@PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return CommonResponse.status(HttpStatus.OK).body(new ChatRequestListDto.Response());
    }

    @GetMapping("/{requestId}")
    @Operation(summary = "채팅 요청 상세 조회")
    public CommonResponse<ChatRequestDetailDto.Response> findChatRequest(@PathVariable Long requestId) {
        return CommonResponse.status(HttpStatus.OK).body(new ChatRequestDetailDto.Response());
    }

    @PostMapping("")
    @Operation(summary = "채팅 요청 저장")
    public CommonResponse<ChatRequestDto.Response> saveChatRequest(
            @AuthenticationPrincipal PrincipalDetails details,
            @Valid @RequestBody ChatRequestDto.Request request
    ) {
        Long requesterId = details.getMember().getId();
        Response response = chatRequestService.saveChatRequest(requesterId, request);
        return CommonResponse.status(HttpStatus.OK).body(response);
    }


    @PostMapping("/{requestId}/accept")
    @Operation(summary = "채팅 요청 수락")
    public CommonResponse<ChatRequestDto.Response> saveChatRequestAccept(
            @AuthenticationPrincipal PrincipalDetails details,
            @PathVariable Long requestId
    ) {
        Long memberId = details.getMember().getId();
        Response response = chatRequestService.acceptRequired(memberId, requestId);
        return CommonResponse.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/{requestId}/reject")
    @Operation(summary = "채팅 요청 거절")
    public CommonResponse<ChatRequestDto.Response> saveChatRequestReject(
            @AuthenticationPrincipal PrincipalDetails details,
            @PathVariable Long requestId
    ) {
        Long memberId = details.getMember().getId();
        Response response = chatRequestService.rejectRequired(memberId, requestId);
        return CommonResponse.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/{requestId}/cancel")
    @Operation(summary = "채팅 요청 취소")
    public CommonResponse<ChatRequestDto.Response> saveChatRequestCancel(
            @AuthenticationPrincipal PrincipalDetails details,
            @PathVariable Long requestId
    ) {
        Long memberId = details.getMember().getId();
        Response response = chatRequestService.cancelRequired(memberId, requestId);
        return CommonResponse.status(HttpStatus.OK).body(response);
    }
}

