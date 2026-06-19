package org.example.knockin.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.example.knockin.dto.ChatMessageDto;
import org.example.knockin.dto.ChatRoomDto;
import org.example.knockin.dto.ChatRoomImageDto;
import org.example.knockin.dto.ChatRoomLeftEvent;
import org.example.knockin.dto.ChatRoomListDto;
import org.example.knockin.dto.EventType;
import org.example.knockin.dto.MessageType;
import org.example.knockin.entity.chat.ChatRoomMember;
import org.example.knockin.entity.chat.ChattingRequiredStatus;
import org.example.knockin.entity.file.File;
import org.example.knockin.entity.file.FileType;
import org.example.knockin.global.exception.BusinessException;
import org.example.knockin.global.exception.ChattingErrorCode;
import org.example.knockin.global.exception.FileErrorCode;
import org.example.knockin.repository.chat.ChatRoomMemberRepository;
import org.example.knockin.repository.chat.ChattingRoomRepository;
import org.example.knockin.repository.file.FileRepository;
import org.example.knockin.service.FileService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
@DisplayName("채팅 서비스")
class ChatServiceImplTest {

    @Mock
    private ChattingRoomRepository chattingRoomRepository;

    @Mock
    private ChatRoomMemberRepository chatRoomMemberRepository;

    @Mock
    private SimpMessageSendingOperations messagingTemplate;

    @Mock
    private ApplicationEventPublisher publisher;

    @Mock
    private ChatRoomAccessService chatRoomAccessService;

    @Mock
    private FileService fileService;

    @Mock
    private FileRepository fileRepository;

    @InjectMocks
    private ChatServiceImpl chatService;

    @Test
    @DisplayName("회원 식별자로 채팅방 목록을 조회하고 결과를 그대로 반환한다")
    void getChatRoomListReturnsRoomsByMemberId() {
        // Given
        Long memberId = 1L;
        List<ChatRoomListDto.Response> chatRooms = List.of(
                chatRoom(10L, "상대방A", "profile-a.jpg", LocalDateTime.of(2026, 6, 18, 10, 0), ChattingRequiredStatus.ACCEPTED),
                chatRoom(20L, "상대방B", "profile-b.jpg", LocalDateTime.of(2026, 6, 18, 11, 0), ChattingRequiredStatus.PENDING)
        );
        when(chattingRoomRepository.findByMemberId(memberId)).thenReturn(chatRooms);

        // When
        List<ChatRoomListDto.Response> responses = chatService.getChatRoomList(memberId);

        // Then
        assertThat(responses).isSameAs(chatRooms);
        assertThat(responses).extracting(ChatRoomListDto.Response::getChatRoomId)
                .containsExactly(10L, 20L);
        assertThat(responses).extracting(ChatRoomListDto.Response::getMemberName)
                .containsExactly("상대방A", "상대방B");
        assertThat(responses).extracting(ChatRoomListDto.Response::getMemberProfileImageUrl)
                .containsExactly("profile-a.jpg", "profile-b.jpg");
        assertThat(responses).extracting(ChatRoomListDto.Response::getStatus)
                .containsExactly(ChattingRequiredStatus.ACCEPTED, ChattingRequiredStatus.PENDING);
        verify(chattingRoomRepository).findByMemberId(memberId);
    }

    @Test
    @DisplayName("참여한 채팅방이 없으면 빈 목록을 반환한다")
    void getChatRoomListReturnsEmptyListWhenMemberHasNoRooms() {
        // Given
        Long memberId = 1L;
        when(chattingRoomRepository.findByMemberId(memberId)).thenReturn(List.of());

        // When
        List<ChatRoomListDto.Response> responses = chatService.getChatRoomList(memberId);

        // Then
        assertThat(responses).isEmpty();
        verify(chattingRoomRepository).findByMemberId(memberId);
    }

    @Test
    @DisplayName("메시지 전송 시 발신자 식별자로 채팅방 구독 채널에 발행한다")
    void sendMessagePublishesChatMessageToRoomDestination() {
        // Given
        Long chatId = 10L;
        Long senderId = 1L;
        ChatMessageDto.Request request = new ChatMessageDto.Request();
        request.setClientMessageId("client-message-id");
        request.setType(MessageType.TEXT);
        request.setMessage("안녕하세요");
        // When
        chatService.sendMessage(chatId, request, senderId);

        // Then
        verify(chatRoomAccessService).checkCanSendMessage(chatId, senderId);
        ArgumentCaptor<Object> payloadCaptor = ArgumentCaptor.forClass(Object.class);
        verify(messagingTemplate).convertAndSend(eq("/sub/chats/10"), payloadCaptor.capture());

        ChatMessageDto.Response response = (ChatMessageDto.Response) payloadCaptor.getValue();
        assertThat(response.getEventType()).isEqualTo(EventType.CHAT_MESSAGE);
        assertThat(response.getChatRoomId()).isEqualTo(chatId);
        assertThat(response.getClientMessageId()).isEqualTo("client-message-id");
        assertThat(response.getSenderId()).isEqualTo(senderId);
        assertThat(response.getType()).isEqualTo(MessageType.TEXT);
        assertThat(response.getMessage()).isEqualTo("안녕하세요");
        assertThat(response.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("이미지 메시지 전송 시 이미지 URL 목록을 구독 채널에 발행한다")
    void sendMessagePublishesImageMessageWithImageUrls() {
        // Given
        Long chatId = 10L;
        Long senderId = 1L;
        ChatMessageDto.Request request = new ChatMessageDto.Request();
        request.setClientMessageId("client-message-id");
        request.setType(MessageType.IMAGE);
        request.setImageUrls(List.of("first.jpg", "second.jpg"));

        // When
        chatService.sendMessage(chatId, request, senderId);

        // Then
        verify(chatRoomAccessService).checkCanSendMessage(chatId, senderId);
        ArgumentCaptor<Object> payloadCaptor = ArgumentCaptor.forClass(Object.class);
        verify(messagingTemplate).convertAndSend(eq("/sub/chats/10"), payloadCaptor.capture());

        ChatMessageDto.Response response = (ChatMessageDto.Response) payloadCaptor.getValue();
        assertThat(response.getEventType()).isEqualTo(EventType.CHAT_MESSAGE);
        assertThat(response.getChatRoomId()).isEqualTo(chatId);
        assertThat(response.getClientMessageId()).isEqualTo("client-message-id");
        assertThat(response.getSenderId()).isEqualTo(senderId);
        assertThat(response.getType()).isEqualTo(MessageType.IMAGE);
        assertThat(response.getImageUrls()).containsExactly("first.jpg", "second.jpg");
        assertThat(response.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("채팅방에 참여 중인 멤버가 아니면 메시지를 발행하지 않는다")
    void sendMessageRejectsMemberWhoIsNotActiveRoomMember() {
        // Given
        Long chatId = 10L;
        Long senderId = 1L;
        ChatMessageDto.Request request = textMessageRequest();
        doThrow(new BusinessException(ChattingErrorCode.ROOM_MEMBER_NOT_FOUND))
                .when(chatRoomAccessService)
                .checkCanSendMessage(chatId, senderId);

        // When & Then
        assertThatThrownBy(() -> chatService.sendMessage(chatId, request, senderId))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getErrorCode()).isEqualTo(ChattingErrorCode.ROOM_MEMBER_NOT_FOUND));
        verifyNoInteractions(messagingTemplate);
    }

    @Test
    @DisplayName("텍스트 메시지 본문이 없으면 메시지를 발행하지 않는다")
    void sendMessageRejectsTextMessageWithoutMessage() {
        // Given
        ChatMessageDto.Request request = new ChatMessageDto.Request();
        request.setClientMessageId("client-message-id");
        request.setType(MessageType.TEXT);

        // When & Then
        assertThatThrownBy(() -> chatService.sendMessage(10L, request, 1L))
                .isInstanceOf(BusinessException.class);
        verifyNoInteractions(chatRoomAccessService, messagingTemplate);
    }

    @Test
    @DisplayName("이미지 메시지 URL이 없으면 메시지를 발행하지 않는다")
    void sendMessageRejectsImageMessageWithoutImageUrls() {
        // Given
        ChatMessageDto.Request request = new ChatMessageDto.Request();
        request.setClientMessageId("client-message-id");
        request.setType(MessageType.IMAGE);

        // When & Then
        assertThatThrownBy(() -> chatService.sendMessage(10L, request, 1L))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getErrorCode()).isEqualTo(ChattingErrorCode.MESSAGE_PAYLOAD_INVALID));
        verifyNoInteractions(chatRoomAccessService, messagingTemplate);
    }

    @Test
    @DisplayName("이미지 메시지 URL 목록이 비어 있으면 메시지를 발행하지 않는다")
    void sendMessageRejectsImageMessageWithEmptyImageUrls() {
        // Given
        ChatMessageDto.Request request = new ChatMessageDto.Request();
        request.setClientMessageId("client-message-id");
        request.setType(MessageType.IMAGE);
        request.setImageUrls(List.of());

        // When & Then
        assertThatThrownBy(() -> chatService.sendMessage(10L, request, 1L))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getErrorCode()).isEqualTo(ChattingErrorCode.MESSAGE_PAYLOAD_INVALID));
        verifyNoInteractions(chatRoomAccessService, messagingTemplate);
    }

    @Test
    @DisplayName("이미지 메시지 URL 목록에 빈 값이 있으면 메시지를 발행하지 않는다")
    void sendMessageRejectsImageMessageWithBlankImageUrl() {
        // Given
        ChatMessageDto.Request request = new ChatMessageDto.Request();
        request.setClientMessageId("client-message-id");
        request.setType(MessageType.IMAGE);
        request.setImageUrls(List.of("first.jpg", " "));

        // When & Then
        assertThatThrownBy(() -> chatService.sendMessage(10L, request, 1L))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getErrorCode()).isEqualTo(ChattingErrorCode.MESSAGE_PAYLOAD_INVALID));
        verifyNoInteractions(chatRoomAccessService, messagingTemplate);
    }

    @Test
    @DisplayName("채팅방 이미지 업로드 시 채팅방 권한을 검증하고 저장된 이미지 URL 목록을 반환한다")
    void uploadImageMessageValidatesRoomAccessAndReturnsImageUrls() throws IOException {
        // Given
        Long chatRoomId = 10L;
        Long memberId = 1L;
        MultipartFile firstMultipartFile = imageFile();
        MultipartFile secondMultipartFile = imageFile();
        File firstFile = chatImageFile("first.jpg");
        File secondFile = chatImageFile("second.jpg");

        when(fileService.upload(firstMultipartFile, FileType.CHAT_ROOM_IMAGE)).thenReturn(firstFile);
        when(fileService.upload(secondMultipartFile, FileType.CHAT_ROOM_IMAGE)).thenReturn(secondFile);
        when(fileRepository.save(firstFile)).thenReturn(firstFile);
        when(fileRepository.save(secondFile)).thenReturn(secondFile);

        // When
        ChatRoomImageDto.Response response = chatService.uploadImageMessage(memberId, chatRoomId, List.of(firstMultipartFile, secondMultipartFile));

        // Then
        verify(chatRoomAccessService).checkCanSendMessage(chatRoomId, memberId);
        assertThat(response.getImageUrls()).containsExactly("first.jpg", "second.jpg");
    }

    @Test
    @DisplayName("채팅방 이미지 업로드 파일 목록이 없으면 업로드하지 않는다")
    void uploadImageMessageRejectsEmptyFiles() {
        // Given
        Long chatRoomId = 10L;
        Long memberId = 1L;

        // When & Then
        assertThatThrownBy(() -> chatService.uploadImageMessage(memberId, chatRoomId, List.of()))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getErrorCode()).isEqualTo(FileErrorCode.FILE_EMPTY));
        verify(chatRoomAccessService).checkCanSendMessage(chatRoomId, memberId);
        verifyNoInteractions(fileService, fileRepository);
    }

    @Test
    @DisplayName("채팅방 이미지 업로드 파일 목록에 빈 파일이 있으면 업로드하지 않는다")
    void uploadImageMessageRejectsEmptyFile() {
        // Given
        Long chatRoomId = 10L;
        Long memberId = 1L;
        MultipartFile emptyFile = mock(MultipartFile.class);
        when(emptyFile.isEmpty()).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> chatService.uploadImageMessage(memberId, chatRoomId, List.of(emptyFile)))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getErrorCode()).isEqualTo(FileErrorCode.FILE_EMPTY));
        verify(chatRoomAccessService).checkCanSendMessage(chatRoomId, memberId);
        verifyNoInteractions(fileService, fileRepository);
    }

    @Test
    @DisplayName("채팅방 이미지 업로드 파일이 10개를 초과하면 업로드하지 않는다")
    void uploadImageMessageRejectsTooManyFiles() {
        // Given
        Long chatRoomId = 10L;
        Long memberId = 1L;
        List<MultipartFile> files = java.util.stream.IntStream.range(0, 11)
                .mapToObj(i -> mock(MultipartFile.class))
                .toList();

        // When & Then
        assertThatThrownBy(() -> chatService.uploadImageMessage(memberId, chatRoomId, files))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getErrorCode()).isEqualTo(FileErrorCode.FILE_COUNT_EXCEEDED));
        verify(chatRoomAccessService).checkCanSendMessage(chatRoomId, memberId);
        verifyNoInteractions(fileService, fileRepository);
    }

    @Test
    @DisplayName("채팅방 이미지 업로드 중 실패하면 현재까지 저장한 파일 DB row를 삭제한다")
    void uploadImageMessageDeletesSavedRowsWhenUploadFails() throws IOException {
        // Given
        Long chatRoomId = 10L;
        Long memberId = 1L;
        MultipartFile firstMultipartFile = imageFile();
        MultipartFile secondMultipartFile = imageFile();
        File firstFile = chatImageFile("first.jpg");

        when(fileService.upload(firstMultipartFile, FileType.CHAT_ROOM_IMAGE)).thenReturn(firstFile);
        when(fileRepository.save(firstFile)).thenReturn(firstFile);
        when(fileService.upload(secondMultipartFile, FileType.CHAT_ROOM_IMAGE)).thenThrow(new IOException("upload failed"));

        // When & Then
        assertThatThrownBy(() -> chatService.uploadImageMessage(memberId, chatRoomId, List.of(firstMultipartFile, secondMultipartFile)))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getErrorCode()).isEqualTo(FileErrorCode.FILE_UPLOAD_FAILED));
        verify(chatRoomAccessService).checkCanSendMessage(chatRoomId, memberId);
        verify(fileRepository).deleteAll(List.of(firstFile));
    }

    @Test
    @DisplayName("활성 채팅방 멤버가 채팅방을 나가면 나간 상태로 변경하고 퇴장 이벤트를 발행 요청한다")
    void leaveChatRoomMarksMemberAsLeftAndPublishesUserLeftEvent() {
        // Given
        Long chatRoomId = 10L;
        Long memberId = 1L;
        ChatRoomMember roomMember = ChatRoomMember.builder()
                .isLeft(false)
                .build();
        when(chatRoomMemberRepository.findActiveMemberByRoomIdAndMemberId(chatRoomId, memberId))
                .thenReturn(Optional.of(roomMember));

        // When
        ChatRoomDto.Response result = chatService.leaveChatRoom(memberId, chatRoomId);

        // Then
        assertThat(roomMember.getIsLeft()).isTrue();
        assertThat(result.getUpdatedAt()).isNotNull();

        ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);
        verify(publisher).publishEvent(eventCaptor.capture());
        verifyNoInteractions(messagingTemplate);

        ChatRoomLeftEvent event = (ChatRoomLeftEvent) eventCaptor.getValue();
        assertThat(event.memberId()).isEqualTo(memberId);
        assertThat(event.chatRoomId()).isEqualTo(chatRoomId);
        assertThat(event.leftAt()).isEqualTo(result.getUpdatedAt());
    }

    @Test
    @DisplayName("채팅방 나가기 이벤트가 커밋된 후 퇴장 이벤트를 구독 채널로 발행한다")
    void handleChatRoomLeftPublishesUserLeftEventToRoomDestination() {
        // Given
        Long chatRoomId = 10L;
        Long memberId = 1L;
        LocalDateTime leftAt = LocalDateTime.of(2026, 6, 19, 21, 50);
        ChatRoomLeftEvent event = new ChatRoomLeftEvent(memberId, chatRoomId, leftAt);

        // When
        chatService.handleChatRoomLeft(event);

        // Then
        ArgumentCaptor<Object> payloadCaptor = ArgumentCaptor.forClass(Object.class);
        verify(messagingTemplate).convertAndSend(eq("/sub/chats/10"), payloadCaptor.capture());

        ChatMessageDto.Response response = (ChatMessageDto.Response) payloadCaptor.getValue();
        assertThat(response.getEventType()).isEqualTo(EventType.USER_LEFT);
        assertThat(response.getChatRoomId()).isEqualTo(chatRoomId);
        assertThat(response.getSenderId()).isEqualTo(memberId);
        assertThat(response.getCreatedAt()).isEqualTo(leftAt);
    }

    @Test
    @DisplayName("활성 채팅방 멤버가 아니면 채팅방 나가기를 거부하고 퇴장 이벤트를 발행하지 않는다")
    void leaveChatRoomRejectsMemberWhoIsNotActiveRoomMember() {
        // Given
        Long chatRoomId = 10L;
        Long memberId = 1L;
        when(chatRoomMemberRepository.findActiveMemberByRoomIdAndMemberId(chatRoomId, memberId))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> chatService.leaveChatRoom(memberId, chatRoomId))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getErrorCode()).isEqualTo(ChattingErrorCode.ROOM_MEMBER_NOT_FOUND));
        verifyNoInteractions(publisher, messagingTemplate);
    }

    private ChatRoomListDto.Response chatRoom(
            Long chatRoomId,
            String memberName,
            String memberProfileImageUrl,
            LocalDateTime createdAt,
            ChattingRequiredStatus status
    ) {
        ChatRoomListDto.Response response = new ChatRoomListDto.Response();
        response.setChatRoomId(chatRoomId);
        response.setMemberName(memberName);
        response.setMemberProfileImageUrl(memberProfileImageUrl);
        response.setCreatedAt(createdAt);
        response.setStatus(status);
        return response;
    }

    private ChatMessageDto.Request textMessageRequest() {
        ChatMessageDto.Request request = new ChatMessageDto.Request();
        request.setClientMessageId("client-message-id");
        request.setType(MessageType.TEXT);
        request.setMessage("안녕하세요");
        return request;
    }

    private MultipartFile imageFile() {
        MultipartFile multipartFile = mock(MultipartFile.class);
        when(multipartFile.isEmpty()).thenReturn(false);
        return multipartFile;
    }

    private File chatImageFile(String savedFileName) {
        return File.builder()
                .type(FileType.CHAT_ROOM_IMAGE)
                .originalFileName(savedFileName)
                .savedFileName(savedFileName)
                .fileExt("jpg")
                .build();
    }
}
