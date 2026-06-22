package org.example.knockin.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.knockin.dto.ChatMessageDto;
import org.example.knockin.dto.ChatRoomDto;
import org.example.knockin.dto.ChatRoomDto.Response;
import org.example.knockin.dto.ChatRoomListDto;
import org.example.knockin.dto.ChatRoomLeftEvent;
import org.example.knockin.dto.ChatRoomMessageEvent;
import org.example.knockin.entity.chat.ChatRoomFile;
import org.example.knockin.entity.chat.ChatRoomMember;
import org.example.knockin.entity.chat.ChatRoomMessage;
import org.example.knockin.entity.file.File;
import org.example.knockin.entity.file.FileType;
import org.example.knockin.global.exception.BusinessException;
import org.example.knockin.global.exception.ChattingErrorCode;
import org.example.knockin.global.exception.FileErrorCode;
import org.example.knockin.repository.chat.ChatRoomFileRepository;
import org.example.knockin.repository.chat.ChatRoomMemberRepository;
import org.example.knockin.repository.chat.ChatRoomMessageRepository;
import org.example.knockin.repository.chat.ChattingRoomRepository;
import org.example.knockin.repository.file.FileRepository;
import org.example.knockin.service.FileService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl {

    public static String IMAGE_MESSAGE_CONTENTS = "사진을 보냈습니다.";

    private final ChattingRoomRepository chattingRoomRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final SimpMessageSendingOperations messagingTemplate;
    private final ApplicationEventPublisher publisher;
    private final ChatRoomAccessService chatRoomAccessService;
    private final ChatRoomMessageRepository chatRoomMessageRepository;
    private final FileService fileService;
    private final FileRepository fileRepository;
    private final ChatRoomFileRepository chatRoomFileRepository;

    public List<ChatRoomListDto.Response> getChatRoomList(Long memberId) {
        return chattingRoomRepository.findByMemberId(memberId);
    }

    @Transactional
    public ChatRoomDto.Response leaveChatRoom(Long memberId, Long chatRoomId) {
        ChatRoomMember roomMember = chatRoomMemberRepository.findActiveMemberByRoomIdAndMemberId(chatRoomId, memberId)
                .orElseThrow(() -> new BusinessException(ChattingErrorCode.ROOM_MEMBER_NOT_FOUND));
        roomMember.left();

        LocalDateTime now = LocalDateTime.now();
        publisher.publishEvent(new ChatRoomLeftEvent(memberId, chatRoomId, now));
        return new Response(now);
    }

    @Transactional
    public void sendMessage(Long chatRoomId, ChatMessageDto.Request request, Long senderId) {
        validateMessageRequest(request);
        ChatRoomMember chatRoomMember = chatRoomMemberRepository.findActiveMemberByRoomIdAndMemberId(chatRoomId, senderId)
                .orElseThrow(() -> new BusinessException(ChattingErrorCode.ROOM_MEMBER_NOT_FOUND));

        switch (request.getType()) {
            case TEXT -> {
                saveMessage(request.getMessage(), chatRoomMember);
                publishMessageEvent(chatRoomId, senderId, request);
            }
            case IMAGE -> {
                File file = findFile(request.getImageUrl());
                ChatRoomMessage chatRoomMessage = saveMessage(IMAGE_MESSAGE_CONTENTS, chatRoomMember);
                saveMessageFile(file, chatRoomMessage);
                publishMessageEvent(chatRoomId, senderId, request);
            }
            default -> throw new BusinessException(ChattingErrorCode.MESSAGE_PAYLOAD_INVALID);
        }
    }

    private ChatRoomMessage saveMessage(String contents, ChatRoomMember chatRoomMember) {
        ChatRoomMessage chatRoomMessage = ChatRoomMessage.builder()
                .contents(contents)
                .chatRoomMember(chatRoomMember)
                .build();

        return chatRoomMessageRepository.save(chatRoomMessage);
    }

    private File findFile(String fileUrl) {
        return fileRepository.findBySavedFileNameAndType(fileUrl, FileType.CHAT_ROOM_IMAGE)
                .orElseThrow(() -> new BusinessException(FileErrorCode.FILE_NOT_FOUND));
    }

    private ChatRoomFile saveMessageFile(File file, ChatRoomMessage chatRoomMessage) {
        ChatRoomFile chatRoomFile = ChatRoomFile.builder()
                .file(file)
                .chatRoomMessage(chatRoomMessage)
                .build();

        return chatRoomFileRepository.save(chatRoomFile);
    }

    private void publishMessageEvent(Long chatRoomId, Long senderId, ChatMessageDto.Request request) {
        ChatRoomMessageEvent event = ChatRoomMessageEvent.builder()
                .chatRoomId(chatRoomId)
                .senderId(senderId)
                .clientMessageId(request.getClientMessageId())
                .messageType(request.getType())
                .message(request.getMessage())
                .imageUrl(request.getImageUrl())
                .build();

        publisher.publishEvent(event);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleChatRoomLeft(ChatRoomLeftEvent event) {
        ChatMessageDto.Response response = ChatMessageDto.Response.userLeft(event.chatRoomId(), event.memberId(), event.leftAt());
        messagingTemplate.convertAndSend("/sub/chats/" + event.chatRoomId(), response);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleMessageSend(ChatRoomMessageEvent event) {
        ChatMessageDto.Response response = ChatMessageDto.Response.chatMessage(event);
        messagingTemplate.convertAndSend("/sub/chats/" + event.chatRoomId(), response);
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
