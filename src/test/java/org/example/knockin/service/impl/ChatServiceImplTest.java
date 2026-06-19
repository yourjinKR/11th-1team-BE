package org.example.knockin.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.example.knockin.dto.ChatMessageDto;
import org.example.knockin.dto.ChatRoomDto;
import org.example.knockin.dto.ChatRoomLeftEvent;
import org.example.knockin.dto.ChatRoomListDto;
import org.example.knockin.dto.EventType;
import org.example.knockin.dto.MessageType;
import org.example.knockin.entity.auth.LoginProviderType;
import org.example.knockin.entity.chat.ChatRoomMember;
import org.example.knockin.entity.chat.ChattingRequiredStatus;
import org.example.knockin.entity.member.Member;
import org.example.knockin.entity.member.MemberRole;
import org.example.knockin.global.auth.dto.PrincipalDetails;
import org.example.knockin.global.exception.BusinessException;
import org.example.knockin.global.exception.ChattingErrorCode;
import org.example.knockin.repository.chat.ChatRoomMemberRepository;
import org.example.knockin.repository.chat.ChattingRoomRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

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
    @DisplayName("메시지 전송 시 인증 주체에서 발신자 식별자를 추출해 채팅방 구독 채널로 발행한다")
    void sendMessagePublishesChatMessageToRoomDestination() {
        // Given
        Long chatId = 10L;
        ChatMessageDto.Request request = new ChatMessageDto.Request();
        request.setClientMessageId("client-message-id");
        request.setType(MessageType.TEXT);
        request.setMessage("안녕하세요");

        UsernamePasswordAuthenticationToken authentication = authentication(1L);
        when(chatRoomMemberRepository.existsActiveMember(chatId, 1L)).thenReturn(true);

        // When
        chatService.sendMessage(chatId, request, authentication);

        // Then
        ArgumentCaptor<Object> payloadCaptor = ArgumentCaptor.forClass(Object.class);
        verify(messagingTemplate).convertAndSend(eq("/sub/chats/10"), payloadCaptor.capture());

        ChatMessageDto.Response response = (ChatMessageDto.Response) payloadCaptor.getValue();
        assertThat(response.getEventType()).isEqualTo(EventType.CHAT_MESSAGE);
        assertThat(response.getClientMessageId()).isEqualTo("client-message-id");
        assertThat(response.getSenderId()).isEqualTo(1L);
        assertThat(response.getType()).isEqualTo(MessageType.TEXT);
        assertThat(response.getMessage()).isEqualTo("안녕하세요");
        assertThat(response.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("채팅방에 참여 중인 멤버가 아니면 메시지를 발행하지 않는다")
    void sendMessageRejectsMemberWhoIsNotActiveRoomMember() {
        // Given
        Long chatId = 10L;
        ChatMessageDto.Request request = textMessageRequest();
        when(chatRoomMemberRepository.existsActiveMember(chatId, 1L)).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> chatService.sendMessage(chatId, request, authentication(1L)))
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
        assertThatThrownBy(() -> chatService.sendMessage(10L, request, authentication(1L)))
                .isInstanceOf(BusinessException.class);
        verifyNoInteractions(messagingTemplate);
    }

    @Test
    @DisplayName("이미지 메시지 URL이 없으면 메시지를 발행하지 않는다")
    void sendMessageRejectsImageMessageWithoutImageUrl() {
        // Given
        ChatMessageDto.Request request = new ChatMessageDto.Request();
        request.setClientMessageId("client-message-id");
        request.setType(MessageType.IMAGE);

        // When & Then
        assertThatThrownBy(() -> chatService.sendMessage(10L, request, authentication(1L)))
                .isInstanceOf(BusinessException.class);
        verifyNoInteractions(messagingTemplate);
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

    private UsernamePasswordAuthenticationToken authentication(Long memberId) {
        Member member = Member.builder()
                .id(memberId)
                .providerType(LoginProviderType.KAKAO)
                .providerId("provider-id")
                .role(MemberRole.USER)
                .build();
        PrincipalDetails details = new PrincipalDetails(member);
        return new UsernamePasswordAuthenticationToken(details, null, details.getAuthorities());
    }

    private ChatMessageDto.Request textMessageRequest() {
        ChatMessageDto.Request request = new ChatMessageDto.Request();
        request.setClientMessageId("client-message-id");
        request.setType(MessageType.TEXT);
        request.setMessage("안녕하세요");
        return request;
    }
}
