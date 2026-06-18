package org.example.knockin.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;
import org.example.knockin.dto.ChatRequestDto;
import org.example.knockin.entity.board.RoommateBoard;
import org.example.knockin.entity.chat.ChatRoomMember;
import org.example.knockin.entity.chat.ChattingRequired;
import org.example.knockin.entity.chat.ChattingRequiredStatus;
import org.example.knockin.entity.chat.ChattingRoom;
import org.example.knockin.entity.member.Member;
import org.example.knockin.global.exception.BusinessException;
import org.example.knockin.global.exception.ChattingErrorCode;
import org.example.knockin.global.exception.CommonErrorCode;
import org.example.knockin.global.exception.MemberErrorCode;
import org.example.knockin.global.exception.RoommateBoardErrorCode;
import org.example.knockin.repository.board.RoommateBoardRepository;
import org.example.knockin.repository.chat.ChatRoomMemberRepository;
import org.example.knockin.repository.chat.ChattingRequiredRepository;
import org.example.knockin.repository.chat.ChattingRoomRepository;
import org.example.knockin.repository.member.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("채팅 요청 서비스")
class ChatRequestServiceImplTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private ChattingRequiredRepository chattingRequiredRepository;

    @Mock
    private RoommateBoardRepository roommateBoardRepository;

    @Mock
    private ChattingRoomRepository chattingRoomRepository;

    @Mock
    private ChatRoomMemberRepository chatRoomMemberRepository;

    @InjectMocks
    private ChatRequestServiceImpl chatRequestService;

    @Test
    @DisplayName("중복 요청이 아니면 승인된 채팅 요청과 채팅방 및 두 명의 방 멤버를 저장한다")
    void saveChatRequestCreatesAcceptedRequestRoomAndMembers() {
        // Given
        Long requesterId = 1L;
        Long requesteeId = 2L;
        Long boardId = 10L;
        Member requester = Member.builder().id(requesterId).build();
        Member requestee = Member.builder().id(requesteeId).build();
        RoommateBoard roommateBoard = RoommateBoard.builder().id(boardId).build();
        ChatRequestDto.Request request = chatRequest(requesteeId, boardId);

        when(memberRepository.findById(requesterId)).thenReturn(Optional.of(requester));
        when(memberRepository.findById(requesteeId)).thenReturn(Optional.of(requestee));
        when(chattingRequiredRepository.existsBetweenMembers(requester, requestee)).thenReturn(false);
        when(roommateBoardRepository.findById(boardId)).thenReturn(Optional.of(roommateBoard));
        when(chattingRequiredRepository.save(any(ChattingRequired.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(chattingRoomRepository.save(any(ChattingRoom.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        ChatRequestDto.Response response = chatRequestService.saveChatRequest(requesterId, request);

        // Then
        ArgumentCaptor<ChattingRequired> requiredCaptor = ArgumentCaptor.forClass(ChattingRequired.class);
        verify(chattingRequiredRepository).save(requiredCaptor.capture());
        ChattingRequired chattingRequired = requiredCaptor.getValue();
        assertThat(chattingRequired.getRequester()).isSameAs(requester);
        assertThat(chattingRequired.getRequestee()).isSameAs(requestee);
        assertThat(chattingRequired.getRoommateBoard()).isSameAs(roommateBoard);
        assertThat(chattingRequired.getStatus()).isEqualTo(ChattingRequiredStatus.ACCEPTED);

        ArgumentCaptor<ChattingRoom> roomCaptor = ArgumentCaptor.forClass(ChattingRoom.class);
        verify(chattingRoomRepository).save(roomCaptor.capture());
        ChattingRoom chattingRoom = roomCaptor.getValue();
        assertThat(chattingRoom.getChattingRequired()).isSameAs(chattingRequired);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Iterable<ChatRoomMember>> membersCaptor = ArgumentCaptor.forClass(Iterable.class);
        verify(chatRoomMemberRepository).saveAll(membersCaptor.capture());
        List<ChatRoomMember> chatRoomMembers = StreamSupport.stream(membersCaptor.getValue().spliterator(), false)
                .toList();
        assertThat(chatRoomMembers).hasSize(2);
        assertThat(chatRoomMembers).extracting(ChatRoomMember::getChattingRoom)
                .containsExactly(chattingRoom, chattingRoom);
        assertThat(chatRoomMembers).extracting(ChatRoomMember::getMember)
                .containsExactly(requester, requestee);
        assertThat(chatRoomMembers).extracting(ChatRoomMember::getIsLeft)
                .containsExactly(false, false);
        assertThat(response.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("게시글이 없는 채팅 요청이면 게시글 조회 없이 승인된 채팅 요청과 채팅방을 저장한다")
    void saveChatRequestCreatesAcceptedRequestRoomWithoutRoommateBoard() {
        // Given
        Long requesterId = 1L;
        Long requesteeId = 2L;
        Member requester = Member.builder().id(requesterId).build();
        Member requestee = Member.builder().id(requesteeId).build();
        ChatRequestDto.Request request = chatRequest(requesteeId, null);

        when(memberRepository.findById(requesterId)).thenReturn(Optional.of(requester));
        when(memberRepository.findById(requesteeId)).thenReturn(Optional.of(requestee));
        when(chattingRequiredRepository.existsBetweenMembers(requester, requestee)).thenReturn(false);
        when(chattingRequiredRepository.save(any(ChattingRequired.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(chattingRoomRepository.save(any(ChattingRoom.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        ChatRequestDto.Response response = chatRequestService.saveChatRequest(requesterId, request);

        // Then
        ArgumentCaptor<ChattingRequired> requiredCaptor = ArgumentCaptor.forClass(ChattingRequired.class);
        verify(chattingRequiredRepository).save(requiredCaptor.capture());
        ChattingRequired chattingRequired = requiredCaptor.getValue();
        assertThat(chattingRequired.getRequester()).isSameAs(requester);
        assertThat(chattingRequired.getRequestee()).isSameAs(requestee);
        assertThat(chattingRequired.getRoommateBoard()).isNull();
        assertThat(chattingRequired.getStatus()).isEqualTo(ChattingRequiredStatus.ACCEPTED);
        verifyNoInteractions(roommateBoardRepository);
        verify(chattingRoomRepository).save(any(ChattingRoom.class));
        verify(chatRoomMemberRepository).saveAll(any());
        assertThat(response.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("요청자 식별자가 없으면 잘못된 요청 예외를 던지고 회원을 조회하지 않는다")
    void saveChatRequestThrowsWhenRequesterIdIsNull() {
        // Given
        ChatRequestDto.Request request = chatRequest(2L, 10L);

        // When & Then
        assertThatThrownBy(() -> chatRequestService.saveChatRequest(null, request))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getErrorCode()).isEqualTo(CommonErrorCode.BAD_REQUEST));
        verifyNoInteractions(memberRepository, chattingRequiredRepository, roommateBoardRepository, chattingRoomRepository, chatRoomMemberRepository);
    }

    @Test
    @DisplayName("요청 본문이 없으면 잘못된 요청 예외를 던지고 회원을 조회하지 않는다")
    void saveChatRequestThrowsWhenRequestBodyIsNull() {
        // When & Then
        assertThatThrownBy(() -> chatRequestService.saveChatRequest(1L, null))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getErrorCode()).isEqualTo(CommonErrorCode.BAD_REQUEST));
        verifyNoInteractions(memberRepository, chattingRequiredRepository, roommateBoardRepository, chattingRoomRepository, chatRoomMemberRepository);
    }

    @Test
    @DisplayName("피요청자 식별자가 없으면 잘못된 요청 예외를 던지고 회원을 조회하지 않는다")
    void saveChatRequestThrowsWhenRequesteeIdIsNull() {
        // Given
        ChatRequestDto.Request request = chatRequest(null, 10L);

        // When & Then
        assertThatThrownBy(() -> chatRequestService.saveChatRequest(1L, request))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getErrorCode()).isEqualTo(CommonErrorCode.BAD_REQUEST));
        verifyNoInteractions(memberRepository, chattingRequiredRepository, roommateBoardRepository, chattingRoomRepository, chatRoomMemberRepository);
    }

    @Test
    @DisplayName("요청자와 피요청자가 같으면 잘못된 요청 예외를 던지고 회원을 조회하지 않는다")
    void saveChatRequestThrowsWhenRequesterAndRequesteeAreSame() {
        // Given
        Long requesterId = 1L;
        ChatRequestDto.Request request = chatRequest(requesterId, 10L);

        // When & Then
        assertThatThrownBy(() -> chatRequestService.saveChatRequest(requesterId, request))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getErrorCode()).isEqualTo(CommonErrorCode.BAD_REQUEST));
        verifyNoInteractions(memberRepository, chattingRequiredRepository, roommateBoardRepository, chattingRoomRepository, chatRoomMemberRepository);
    }

    @Test
    @DisplayName("요청자가 없으면 회원 없음 예외를 던지고 피요청자와 채팅 요청을 조회하지 않는다")
    void saveChatRequestThrowsWhenRequesterDoesNotExist() {
        // Given
        Long requesterId = 1L;
        Long requesteeId = 2L;
        ChatRequestDto.Request request = chatRequest(requesteeId, 10L);
        when(memberRepository.findById(requesterId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> chatRequestService.saveChatRequest(requesterId, request))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getErrorCode()).isEqualTo(MemberErrorCode.MEMBER_NOT_FOUND));
        verify(memberRepository, never()).findById(requesteeId);
        verifyNoInteractions(chattingRequiredRepository, roommateBoardRepository, chattingRoomRepository, chatRoomMemberRepository);
    }

    @Test
    @DisplayName("피요청자가 없으면 회원 없음 예외를 던지고 채팅 요청 중복 여부를 조회하지 않는다")
    void saveChatRequestThrowsWhenRequesteeDoesNotExist() {
        // Given
        Long requesterId = 1L;
        Long requesteeId = 2L;
        Member requester = Member.builder().id(requesterId).build();
        ChatRequestDto.Request request = chatRequest(requesteeId, 10L);
        when(memberRepository.findById(requesterId)).thenReturn(Optional.of(requester));
        when(memberRepository.findById(requesteeId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> chatRequestService.saveChatRequest(requesterId, request))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getErrorCode()).isEqualTo(MemberErrorCode.MEMBER_NOT_FOUND));
        verifyNoInteractions(chattingRequiredRepository, roommateBoardRepository, chattingRoomRepository, chatRoomMemberRepository);
    }

    @Test
    @DisplayName("두 회원 사이에 채팅 요청이 이미 있으면 방향과 무관하게 중복 예외를 던지고 게시글과 채팅방을 저장하지 않는다")
    void saveChatRequestThrowsWhenRequestAlreadyExists() {
        // Given
        Long requesterId = 1L;
        Long requesteeId = 2L;
        Member requester = Member.builder().id(requesterId).build();
        Member requestee = Member.builder().id(requesteeId).build();
        ChatRequestDto.Request request = chatRequest(requesteeId, 10L);
        when(memberRepository.findById(requesterId)).thenReturn(Optional.of(requester));
        when(memberRepository.findById(requesteeId)).thenReturn(Optional.of(requestee));
        when(chattingRequiredRepository.existsBetweenMembers(requester, requestee)).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> chatRequestService.saveChatRequest(requesterId, request))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getErrorCode()).isEqualTo(ChattingErrorCode.REQUIRED_DUPLICATE));
        verify(chattingRequiredRepository, never()).save(any(ChattingRequired.class));
        verifyNoInteractions(roommateBoardRepository, chattingRoomRepository, chatRoomMemberRepository);
    }

    @Test
    @DisplayName("요청한 게시글이 없으면 게시글 없음 예외를 던지고 채팅 요청과 채팅방을 저장하지 않는다")
    void saveChatRequestThrowsWhenRoommateBoardDoesNotExist() {
        // Given
        Long requesterId = 1L;
        Long requesteeId = 2L;
        Long boardId = 10L;
        Member requester = Member.builder().id(requesterId).build();
        Member requestee = Member.builder().id(requesteeId).build();
        ChatRequestDto.Request request = chatRequest(requesteeId, boardId);
        when(memberRepository.findById(requesterId)).thenReturn(Optional.of(requester));
        when(memberRepository.findById(requesteeId)).thenReturn(Optional.of(requestee));
        when(chattingRequiredRepository.existsBetweenMembers(requester, requestee)).thenReturn(false);
        when(roommateBoardRepository.findById(boardId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> chatRequestService.saveChatRequest(requesterId, request))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getErrorCode()).isEqualTo(RoommateBoardErrorCode.ROOMMATE_BOARD_NOT_FOUND));
        verify(chattingRequiredRepository, never()).save(any(ChattingRequired.class));
        verifyNoInteractions(chattingRoomRepository, chatRoomMemberRepository);
    }

    private ChatRequestDto.Request chatRequest(Long requesteeId, Long boardId) {
        ChatRequestDto.Request request = new ChatRequestDto.Request();
        request.setRequesteeId(requesteeId);
        request.setBoardId(boardId);
        return request;
    }
}
