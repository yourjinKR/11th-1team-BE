package org.example.knockin.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import org.example.knockin.dto.ChatRoomListDto;
import org.example.knockin.entity.chat.ChattingRequiredStatus;
import org.example.knockin.repository.chat.ChattingRoomRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("채팅 서비스")
class ChatServiceImplTest {

    @Mock
    private ChattingRoomRepository chattingRoomRepository;

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
}
