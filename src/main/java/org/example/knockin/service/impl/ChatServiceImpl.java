package org.example.knockin.service.impl;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.knockin.dto.ChatMessageDto;
import org.example.knockin.dto.ChatRoomDto;
import org.example.knockin.dto.ChatRoomDto.Response;
import org.example.knockin.dto.ChatRoomImageDto;
import org.example.knockin.dto.ChatRoomListDto;
import org.example.knockin.dto.ChatRoomLeftEvent;
import org.example.knockin.entity.chat.ChatRoomMember;
import org.example.knockin.entity.file.File;
import org.example.knockin.entity.file.FileType;
import org.example.knockin.global.exception.BusinessException;
import org.example.knockin.global.exception.ChattingErrorCode;
import org.example.knockin.global.exception.FileErrorCode;
import org.example.knockin.repository.chat.ChatRoomMemberRepository;
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
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl {
    private static final int CHAT_IMAGE_MAXIMUM = 10;

    private final ChattingRoomRepository chattingRoomRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final SimpMessageSendingOperations messagingTemplate;
    private final ApplicationEventPublisher publisher;
    private final ChatRoomAccessService chatRoomAccessService;
    private final FileService fileService;
    private final FileRepository fileRepository;

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

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleChatRoomLeft(ChatRoomLeftEvent event) {
        ChatMessageDto.Response response = ChatMessageDto.Response.userLeft(event.chatRoomId(), event.memberId(), event.leftAt());
        messagingTemplate.convertAndSend("/sub/chats/" + event.chatRoomId(), response);
    }

    @Transactional(rollbackFor = IOException.class)
    public ChatRoomImageDto.Response uploadImageMessage(Long memberId, Long chatRoomId, List<MultipartFile> multipartFiles) {
        chatRoomAccessService.checkCanSendMessage(chatRoomId, memberId);
        validateChatImageFiles(multipartFiles);

        List<File> files = saveChatRoomImageFiles(multipartFiles);
        List<String> imageUrls = files.stream().map(File::getSavedFileName).toList();

        return ChatRoomImageDto.Response.builder().imageUrls(imageUrls).build();
    }

    private List<File> saveChatRoomImageFiles(List<MultipartFile> files) {
        List<File> fileList = new ArrayList<>();

        try {
            for (MultipartFile multipartFile : files) {
                File file = fileService.upload(multipartFile, FileType.CHAT_ROOM_IMAGE);
                fileList.add(fileRepository.save(file));
            }
        } catch (BusinessException e) {
            fileRepository.deleteAll(fileList);
            throw e;
        } catch (IOException e) {
            fileRepository.deleteAll(fileList);
            throw new BusinessException(FileErrorCode.FILE_UPLOAD_FAILED);
        }

        return fileList;
    }

    private void validateChatImageFiles(List<MultipartFile> multipartFiles) {
        if (multipartFiles == null || multipartFiles.isEmpty()) {
            throw new BusinessException(FileErrorCode.FILE_EMPTY);
        }

        if (multipartFiles.size() > CHAT_IMAGE_MAXIMUM) {
            throw new BusinessException(FileErrorCode.FILE_COUNT_EXCEEDED, CHAT_IMAGE_MAXIMUM);
        }

        if (multipartFiles.stream().anyMatch(file -> file == null || file.isEmpty())) {
            throw new BusinessException(FileErrorCode.FILE_EMPTY);
        }
    }

    public void sendMessage(Long chatId, ChatMessageDto.Request request, Long senderId) {
        validateMessageRequest(request);
        chatRoomAccessService.checkCanSendMessage(chatId, senderId);
        ChatMessageDto.Response response = ChatMessageDto.Response.chatMessage(chatId, senderId, request, LocalDateTime.now());
        messagingTemplate.convertAndSend("/sub/chats/" + chatId, response);
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
        if (request.getImageUrls() == null
                || request.getImageUrls().isEmpty()
                || request.getImageUrls().stream().anyMatch(url -> !StringUtils.hasText(url))) {
            throw new BusinessException(ChattingErrorCode.MESSAGE_PAYLOAD_INVALID);
        }
    }
}
