package org.example.knockin.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.security.Principal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.knockin.dto.ChatMessageDto;
import org.example.knockin.dto.ChatRoomListDto;
import org.example.knockin.dto.ChatRoomListDto.Response;
import org.example.knockin.global.api.CommonResponse;
import org.example.knockin.global.auth.dto.PrincipalDetails;
import org.example.knockin.service.impl.ChatServiceImpl;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/chats")
@RequiredArgsConstructor
@Tag(name = "6. 채팅 (Chat)")
public class ChatController {
    private final ChatServiceImpl chatService;

    @GetMapping("")
    @Operation(summary = "채팅방 목록 조회")
    public CommonResponse<List<ChatRoomListDto.Response>> findChatRoomList(
            @AuthenticationPrincipal PrincipalDetails details
    ) {
        Long memberId = details.getMember().getId();
        List<Response> responses = chatService.getChatRoomList(memberId);
        return CommonResponse.status(HttpStatus.OK).body(responses);
    }


    @MessageMapping("/chats/{chatId}/messages")
    @Operation(summary = "메시지 전송")
    public void sendMessage(
            @DestinationVariable("chatId") Long chatId,
            @Payload ChatMessageDto.Request request,
            Principal principal
    ) {
        chatService.sendMessage(chatId, request, principal);
    }

    @MessageMapping("/chats/{chatId}/leave")
    @Operation(summary = "채팅방 나가기")
    public void leaveChat(@DestinationVariable("chatId") Long chatId, Principal principal) {
        chatService.leaveChat(chatId, principal);
    }
}

