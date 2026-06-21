package org.example.knockin.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
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
import org.example.knockin.dto.ChatRoomMessageEvent;
import org.example.knockin.dto.EventType;
import org.example.knockin.dto.MessageType;
import org.example.knockin.entity.chat.ChatRoomFile;
import org.example.knockin.entity.chat.ChatRoomMember;
import org.example.knockin.entity.chat.ChatRoomMessage;
import org.example.knockin.entity.chat.ChattingRequiredStatus;
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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
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
    private ChatRoomMessageRepository chatRoomMessageRepository;

    @Mock
    private FileService fileService;

    @Mock
    private FileRepository fileRepository;

    @Mock
    private ChatRoomFileRepository chatRoomFileRepository;

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
    @DisplayName("채팅방 이미지 파일을 업로드하고 URL을 반환한다")
    void uploadImageUploadsFileAndReturnsUrl() throws IOException {
        // Given
        Long chatRoomId = 10L;
        Long memberId = 1L;
        MultipartFile multipartFile = multipartFile(false);
        File file = chatImage("chat-image.jpg");
        when(fileService.upload(multipartFile, FileType.CHAT_ROOM_IMAGE)).thenReturn(file);
        when(fileRepository.save(file)).thenReturn(file);

        // When
        ChatRoomImageDto.Response response = chatService.uploadImage(
                chatRoomId,
                memberId,
                multipartFile
        );

        // Then
        assertThat(response.getImageUrl()).isEqualTo("chat-image.jpg");
        InOrder inOrder = inOrder(chatRoomAccessService, fileService, fileRepository);
        inOrder.verify(chatRoomAccessService).checkCanSendMessage(chatRoomId, memberId);
        inOrder.verify(fileService).upload(multipartFile, FileType.CHAT_ROOM_IMAGE);
        inOrder.verify(fileRepository).save(file);
    }

    @Test
    @DisplayName("채팅방 이미지 업로드 요청에 파일이 없으면 실패한다")
    void uploadImageRejectsNullFile() {
        assertThatThrownBy(() -> chatService.uploadImage(10L, 1L, null))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getErrorCode()).isEqualTo(FileErrorCode.FILE_EMPTY));
        verifyNoInteractions(chatRoomAccessService, fileService, fileRepository);
    }

    @Test
    @DisplayName("채팅방 이미지 업로드 요청 파일이 비어 있으면 실패한다")
    void uploadImageRejectsEmptyFile() {
        // Given
        MultipartFile emptyFile = multipartFile(true);

        // When & Then
        assertThatThrownBy(() -> chatService.uploadImage(10L, 1L, emptyFile))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getErrorCode()).isEqualTo(FileErrorCode.FILE_EMPTY));
        verifyNoInteractions(chatRoomAccessService, fileService, fileRepository);
    }

    @Test
    @DisplayName("채팅방 이미지 업로드 중 실패하면 업로드 실패 예외를 던진다")
    void uploadImageThrowsWhenFileUploadFails() throws IOException {
        // Given
        MultipartFile multipartFile = multipartFile(false);
        when(fileService.upload(multipartFile, FileType.CHAT_ROOM_IMAGE))
                .thenThrow(new IOException("upload failed"));

        // When & Then
        assertThatThrownBy(() -> chatService.uploadImage(10L, 1L, multipartFile))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getErrorCode()).isEqualTo(FileErrorCode.FILE_UPLOAD_FAILED));
        verifyNoInteractions(fileRepository);
    }

    @Test
    @DisplayName("텍스트 메시지 전송 시 메시지를 저장하고 커밋 후 발행할 이벤트를 등록한다")
    void sendTextMessageSavesMessageAndPublishesEvent() {
        // Given
        Long chatId = 10L;
        Long senderId = 1L;
        ChatRoomMember roomMember = activeRoomMember();
        ChatMessageDto.Request request = textMessageRequest();
        when(chatRoomMemberRepository.findActiveMemberByRoomIdAndMemberId(chatId, senderId))
                .thenReturn(Optional.of(roomMember));
        when(chatRoomMessageRepository.save(any(ChatRoomMessage.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        chatService.sendMessage(chatId, request, senderId);

        // Then
        ArgumentCaptor<ChatRoomMessage> messageCaptor = ArgumentCaptor.forClass(ChatRoomMessage.class);
        verify(chatRoomMessageRepository).save(messageCaptor.capture());
        assertThat(messageCaptor.getValue().getContents()).isEqualTo("안녕하세요");
        assertThat(messageCaptor.getValue().getChatRoomMember()).isSameAs(roomMember);

        ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);
        verify(publisher).publishEvent(eventCaptor.capture());
        ChatRoomMessageEvent event = (ChatRoomMessageEvent) eventCaptor.getValue();
        assertThat(event.chatRoomId()).isEqualTo(chatId);
        assertThat(event.senderId()).isEqualTo(senderId);
        assertThat(event.clientMessageId()).isEqualTo("client-message-id");
        assertThat(event.messageType()).isEqualTo(MessageType.TEXT);
        assertThat(event.message()).isEqualTo("안녕하세요");
        assertThat(event.imageUrl()).isNull();
        verifyNoInteractions(messagingTemplate);
    }

    @Test
    @DisplayName("이미지 메시지 전송 시 메시지와 파일 연결을 저장하고 커밋 후 발행할 이벤트를 등록한다")
    void sendImageMessageSavesMessageFileAndPublishesEvent() {
        // Given
        Long chatId = 10L;
        Long senderId = 1L;
        ChatRoomMember roomMember = activeRoomMember();
        ChatMessageDto.Request request = imageMessageRequest("chat-image.jpg");
        File file = chatImage("chat-image.jpg");
        ChatRoomMessage savedMessage = ChatRoomMessage.builder()
                .contents("사진을 보냈습니다.")
                .chatRoomMember(roomMember)
                .build();
        when(chatRoomMemberRepository.findActiveMemberByRoomIdAndMemberId(chatId, senderId))
                .thenReturn(Optional.of(roomMember));
        when(fileRepository.findBySavedFileNameAndType("chat-image.jpg", FileType.CHAT_ROOM_IMAGE))
                .thenReturn(Optional.of(file));
        when(chatRoomMessageRepository.save(any(ChatRoomMessage.class))).thenReturn(savedMessage);
        when(chatRoomFileRepository.save(any(ChatRoomFile.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        chatService.sendMessage(chatId, request, senderId);

        // Then
        ArgumentCaptor<ChatRoomMessage> messageCaptor = ArgumentCaptor.forClass(ChatRoomMessage.class);
        verify(chatRoomMessageRepository).save(messageCaptor.capture());
        assertThat(messageCaptor.getValue().getContents()).isEqualTo("사진을 보냈습니다.");
        assertThat(messageCaptor.getValue().getChatRoomMember()).isSameAs(roomMember);

        ArgumentCaptor<ChatRoomFile> chatRoomFileCaptor = ArgumentCaptor.forClass(ChatRoomFile.class);
        verify(chatRoomFileRepository).save(chatRoomFileCaptor.capture());
        assertThat(chatRoomFileCaptor.getValue().getFile()).isSameAs(file);
        assertThat(chatRoomFileCaptor.getValue().getChatRoomMessage()).isSameAs(savedMessage);

        ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);
        verify(publisher).publishEvent(eventCaptor.capture());
        ChatRoomMessageEvent event = (ChatRoomMessageEvent) eventCaptor.getValue();
        assertThat(event.messageType()).isEqualTo(MessageType.IMAGE);
        assertThat(event.imageUrl()).isEqualTo("chat-image.jpg");
        verifyNoInteractions(messagingTemplate);
    }

    @Test
    @DisplayName("이미지 메시지 URL과 일치하는 채팅방 이미지 파일이 없으면 실패한다")
    void sendImageMessageRejectsUnknownImageUrl() {
        // Given
        Long chatId = 10L;
        Long senderId = 1L;
        ChatMessageDto.Request request = imageMessageRequest("unknown.jpg");
        when(chatRoomMemberRepository.findActiveMemberByRoomIdAndMemberId(chatId, senderId))
                .thenReturn(Optional.of(activeRoomMember()));
        when(fileRepository.findBySavedFileNameAndType("unknown.jpg", FileType.CHAT_ROOM_IMAGE))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> chatService.sendMessage(chatId, request, senderId))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getErrorCode()).isEqualTo(FileErrorCode.FILE_NOT_FOUND));
        verifyNoInteractions(chatRoomMessageRepository, chatRoomFileRepository, publisher, messagingTemplate);
    }

    @Test
    @DisplayName("채팅방에 참여 중인 멤버가 아니면 메시지를 저장하지 않는다")
    void sendMessageRejectsMemberWhoIsNotActiveRoomMember() {
        // Given
        Long chatId = 10L;
        Long senderId = 1L;
        ChatMessageDto.Request request = textMessageRequest();
        when(chatRoomMemberRepository.findActiveMemberByRoomIdAndMemberId(chatId, senderId))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> chatService.sendMessage(chatId, request, senderId))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getErrorCode()).isEqualTo(ChattingErrorCode.ROOM_MEMBER_NOT_FOUND));
        verifyNoInteractions(chatRoomMessageRepository, chatRoomFileRepository, publisher, messagingTemplate);
    }

    @Test
    @DisplayName("텍스트 메시지 본문이 없으면 메시지를 저장하지 않는다")
    void sendMessageRejectsTextMessageWithoutMessage() {
        // Given
        ChatMessageDto.Request request = new ChatMessageDto.Request();
        request.setClientMessageId("client-message-id");
        request.setType(MessageType.TEXT);

        // When & Then
        assertThatThrownBy(() -> chatService.sendMessage(10L, request, 1L))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getErrorCode()).isEqualTo(ChattingErrorCode.MESSAGE_PAYLOAD_INVALID));
        verifyNoInteractions(chatRoomMemberRepository, chatRoomMessageRepository, messagingTemplate);
    }

    @Test
    @DisplayName("이미지 메시지 URL이 없으면 메시지를 저장하지 않는다")
    void sendMessageRejectsImageMessageWithoutImageUrl() {
        // Given
        ChatMessageDto.Request request = new ChatMessageDto.Request();
        request.setClientMessageId("client-message-id");
        request.setType(MessageType.IMAGE);

        // When & Then
        assertThatThrownBy(() -> chatService.sendMessage(10L, request, 1L))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getErrorCode()).isEqualTo(ChattingErrorCode.MESSAGE_PAYLOAD_INVALID));
        verifyNoInteractions(chatRoomMemberRepository, chatRoomMessageRepository, messagingTemplate);
    }

    @Test
    @DisplayName("메시지 전송 이벤트가 커밋된 후 채팅 메시지를 구독 채널로 발행한다")
    void handleMessageSendPublishesChatMessageToRoomDestination() {
        // Given
        Long chatId = 10L;
        Long senderId = 1L;
        ChatRoomMessageEvent event = ChatRoomMessageEvent.builder()
                .chatRoomId(chatId)
                .senderId(senderId)
                .clientMessageId("client-message-id")
                .messageType(MessageType.TEXT)
                .message("안녕하세요")
                .build();

        // When
        chatService.handleMessageSend(event);

        // Then
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

    private ChatMessageDto.Request imageMessageRequest(String imageUrl) {
        ChatMessageDto.Request request = new ChatMessageDto.Request();
        request.setClientMessageId("client-message-id");
        request.setType(MessageType.IMAGE);
        request.setImageUrl(imageUrl);
        return request;
    }

    private ChatRoomMember activeRoomMember() {
        return ChatRoomMember.builder()
                .isLeft(false)
                .build();
    }

    private File chatImage(String savedFileName) {
        return File.builder()
                .type(FileType.CHAT_ROOM_IMAGE)
                .originalFileName(savedFileName)
                .savedFileName(savedFileName)
                .fileExt("jpg")
                .isDeleted(false)
                .build();
    }

    private MultipartFile multipartFile(boolean empty) {
        MultipartFile multipartFile = mock(MultipartFile.class);
        when(multipartFile.isEmpty()).thenReturn(empty);
        return multipartFile;
    }
}
