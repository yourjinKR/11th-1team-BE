package org.example.knockin.service.impl;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.knockin.dto.ChatMessageDto;
import org.example.knockin.dto.ChatRoomListDto;
import org.example.knockin.global.auth.dto.PrincipalDetails;
import org.example.knockin.global.auth.exception.AuthErrorCode;
import org.example.knockin.global.auth.exception.AuthException;
import org.example.knockin.global.exception.BusinessException;
import org.example.knockin.global.exception.ChattingErrorCode;
import org.example.knockin.repository.chat.ChatRoomMemberRepository;
import org.example.knockin.repository.chat.ChattingRoomRepository;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl {

    private final ChattingRoomRepository chattingRoomRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final SimpMessageSendingOperations messagingTemplate;

    public List<ChatRoomListDto.Response> getChatRoomList(Long memberId) {
        return chattingRoomRepository.findByMemberId(memberId);
    }

    public void sendMessage(Long chatId, ChatMessageDto.Request request, Principal principal) {
        validateMessageRequest(request);
        Long senderId = extractMemberId(principal);
        validateActiveRoomMember(chatId, senderId);
        ChatMessageDto.Response response = ChatMessageDto.Response.chatMessage(senderId, request, LocalDateTime.now());
        messagingTemplate.convertAndSend("/sub/chats/" + chatId, response);
    }

    public void leaveChat(Long chatId, Principal principal) {
        Long senderId = extractMemberId(principal);
        ChatMessageDto.Response response = ChatMessageDto.Response.userLeft(senderId, LocalDateTime.now());
        messagingTemplate.convertAndSend("/sub/chats/" + chatId, response);
    }

    private Long extractMemberId(Principal principal) {
        if (!(principal instanceof Authentication authentication)
                || !(authentication.getPrincipal() instanceof PrincipalDetails details)) {
            throw new AuthException(AuthErrorCode.AUTHENTICATION_FAILED);
        }

        return details.getMember().getId();
    }

    private void validateActiveRoomMember(Long chatId, Long memberId) {
        boolean exists = chatRoomMemberRepository.existsActiveMember(chatId, memberId);
        if (!exists) {
            throw new BusinessException(ChattingErrorCode.ROOM_MEMBER_NOT_FOUND);
        }
    }

    private void validateMessageRequest(ChatMessageDto.Request request) {
        if (request == null
                || !StringUtils.hasText(request.getClientMessageId())
                || request.getType() == null) {
            throw new BusinessException(ChattingErrorCode.MESSAGE_PAYLOAD_INVALID);
        }

        switch (request.getType()) {
            case TEXT -> validateTextMessage(request);
            case IMAGE -> validateImageMessage(request);
        }
    }

    private void validateTextMessage(ChatMessageDto.Request request) {
        if (!StringUtils.hasText(request.getMessage())) {
            throw new BusinessException(ChattingErrorCode.MESSAGE_PAYLOAD_INVALID);
        }
    }

    private void validateImageMessage(ChatMessageDto.Request request) {
        if (!StringUtils.hasText(request.getImageUrl())) {
            throw new BusinessException(ChattingErrorCode.MESSAGE_PAYLOAD_INVALID);
        }
    }
}
