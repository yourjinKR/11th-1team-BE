package org.example.knockin.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Optional;
import org.example.knockin.dto.ChatRequestDto;
import org.example.knockin.entity.alarm.AlarmType;
import org.example.knockin.entity.board.RoommateBoard;
import org.example.knockin.entity.chat.ChattingRequired;
import org.example.knockin.entity.chat.ChattingRequiredAlarm;
import org.example.knockin.entity.chat.ChattingRequiredStatus;
import org.example.knockin.entity.chat.ChattingRoom;
import org.example.knockin.entity.member.BasicInformation;
import org.example.knockin.entity.member.Gender;
import org.example.knockin.entity.member.Member;
import org.example.knockin.global.exception.BusinessException;
import org.example.knockin.global.exception.CommonErrorCode;
import org.example.knockin.global.exception.MemberErrorCode;
import org.example.knockin.global.exception.RequiredErrorCode;
import org.example.knockin.global.exception.RoommateBoardErrorCode;
import org.example.knockin.repository.board.RoommateBoardRepository;
import org.example.knockin.repository.chat.ChatRoomMemberRepository;
import org.example.knockin.repository.chat.ChattingRequiredAlarmRepository;
import org.example.knockin.repository.chat.ChattingRequiredRepository;
import org.example.knockin.repository.chat.ChattingRoomRepository;
import org.example.knockin.repository.member.BasicInformationRepository;
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

    @Mock
    private AlarmServiceImpl alarmService;

    @Mock
    private ChattingRequiredAlarmRepository chattingRequiredAlarmRepository;

    @Mock
    private BasicInformationRepository basicInformationRepository;

    @InjectMocks
    private ChatRequestServiceImpl chatRequestService;

    @Test
    @DisplayName("중복 요청이 아니면 대기 중인 채팅 요청을 저장하고 채팅방은 생성하지 않는다")
    void saveChatRequestCreatesPendingRequestWithoutChatRoom() {
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
        when(chattingRequiredRepository.findLatest(requester, requestee)).thenReturn(Optional.empty());
        when(roommateBoardRepository.findById(boardId)).thenReturn(Optional.of(roommateBoard));
        when(chattingRequiredRepository.save(any(ChattingRequired.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(basicInformationRepository.findLatestBasicInformation(requester))
                .thenReturn(Optional.of(basicInformation(requester, "김중민")));
        when(chattingRequiredAlarmRepository.save(any(ChattingRequiredAlarm.class)))
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
        assertThat(chattingRequired.getStatus()).isEqualTo(ChattingRequiredStatus.PENDING);
        assertChattingRequiredAlarm(requestee, "김중민님이 매칭을 요청했어요", chattingRequired);
        verify(alarmService).sendToClient(eq(requesteeId), eq(AlarmType.CHATTING_REQUIRED.name()), any(ChattingRequiredAlarm.class));
        verify(chattingRoomRepository, never()).save(any(ChattingRoom.class));
        verifyNoInteractions(chatRoomMemberRepository);
        assertThat(response.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("게시글이 없는 채팅 요청이면 게시글 조회 없이 대기 중인 채팅 요청을 저장한다")
    void saveChatRequestCreatesPendingRequestWithoutRoommateBoard() {
        // Given
        Long requesterId = 1L;
        Long requesteeId = 2L;
        Member requester = Member.builder().id(requesterId).build();
        Member requestee = Member.builder().id(requesteeId).build();
        ChatRequestDto.Request request = chatRequest(requesteeId, null);

        when(memberRepository.findById(requesterId)).thenReturn(Optional.of(requester));
        when(memberRepository.findById(requesteeId)).thenReturn(Optional.of(requestee));
        when(chattingRequiredRepository.findLatest(requester, requestee)).thenReturn(Optional.empty());
        when(chattingRequiredRepository.save(any(ChattingRequired.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(basicInformationRepository.findLatestBasicInformation(requester))
                .thenReturn(Optional.of(basicInformation(requester, "김중민")));
        when(chattingRequiredAlarmRepository.save(any(ChattingRequiredAlarm.class)))
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
        assertThat(chattingRequired.getStatus()).isEqualTo(ChattingRequiredStatus.PENDING);
        assertChattingRequiredAlarm(requestee, "김중민님이 매칭을 요청했어요", chattingRequired);
        verify(alarmService).sendToClient(eq(requesteeId), eq(AlarmType.CHATTING_REQUIRED.name()), any(ChattingRequiredAlarm.class));
        verifyNoInteractions(roommateBoardRepository);
        verify(chattingRoomRepository, never()).save(any(ChattingRoom.class));
        verifyNoInteractions(chatRoomMemberRepository);
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
        verifyNoInteractions(memberRepository, chattingRequiredRepository, roommateBoardRepository, chattingRoomRepository,
                chatRoomMemberRepository, basicInformationRepository, chattingRequiredAlarmRepository, alarmService);
    }

    @Test
    @DisplayName("요청 본문이 없으면 잘못된 요청 예외를 던지고 회원을 조회하지 않는다")
    void saveChatRequestThrowsWhenRequestBodyIsNull() {
        // When & Then
        assertThatThrownBy(() -> chatRequestService.saveChatRequest(1L, null))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getErrorCode()).isEqualTo(CommonErrorCode.BAD_REQUEST));
        verifyNoInteractions(memberRepository, chattingRequiredRepository, roommateBoardRepository, chattingRoomRepository,
                chatRoomMemberRepository, basicInformationRepository, chattingRequiredAlarmRepository, alarmService);
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
        verifyNoInteractions(memberRepository, chattingRequiredRepository, roommateBoardRepository, chattingRoomRepository,
                chatRoomMemberRepository, basicInformationRepository, chattingRequiredAlarmRepository, alarmService);
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
        verifyNoInteractions(memberRepository, chattingRequiredRepository, roommateBoardRepository, chattingRoomRepository,
                chatRoomMemberRepository, basicInformationRepository, chattingRequiredAlarmRepository, alarmService);
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
        verifyNoInteractions(chattingRequiredRepository, roommateBoardRepository, chattingRoomRepository, chatRoomMemberRepository,
                basicInformationRepository, chattingRequiredAlarmRepository, alarmService);
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
        verifyNoInteractions(chattingRequiredRepository, roommateBoardRepository, chattingRoomRepository, chatRoomMemberRepository,
                basicInformationRepository, chattingRequiredAlarmRepository, alarmService);
    }

    @Test
    @DisplayName("두 회원 사이에 대기 중인 채팅 요청이 이미 있으면 중복 예외를 던지고 게시글과 채팅방을 저장하지 않는다")
    void saveChatRequestThrowsWhenPendingRequestAlreadyExists() {
        // Given
        Long requesterId = 1L;
        Long requesteeId = 2L;
        Member requester = Member.builder().id(requesterId).build();
        Member requestee = Member.builder().id(requesteeId).build();
        ChatRequestDto.Request request = chatRequest(requesteeId, 10L);
        ChattingRequired pendingRequest = ChattingRequired.builder()
                .requester(requestee)
                .requestee(requester)
                .status(ChattingRequiredStatus.PENDING)
                .build();
        when(memberRepository.findById(requesterId)).thenReturn(Optional.of(requester));
        when(memberRepository.findById(requesteeId)).thenReturn(Optional.of(requestee));
        when(chattingRequiredRepository.findLatest(requester, requestee)).thenReturn(Optional.of(pendingRequest));

        // When & Then
        assertThatThrownBy(() -> chatRequestService.saveChatRequest(requesterId, request))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getErrorCode()).isEqualTo(RequiredErrorCode.CHATTING_DUPLICATE));
        verify(chattingRequiredRepository, never()).save(any(ChattingRequired.class));
        verify(chattingRoomRepository, never()).save(any(ChattingRoom.class));
        verifyNoInteractions(roommateBoardRepository, chatRoomMemberRepository, basicInformationRepository,
                chattingRequiredAlarmRepository, alarmService);
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
        when(chattingRequiredRepository.findLatest(requester, requestee)).thenReturn(Optional.empty());
        when(roommateBoardRepository.findById(boardId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> chatRequestService.saveChatRequest(requesterId, request))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getErrorCode()).isEqualTo(RoommateBoardErrorCode.ROOMMATE_BOARD_NOT_FOUND));
        verify(chattingRequiredRepository, never()).save(any(ChattingRequired.class));
        verify(chattingRoomRepository, never()).save(any(ChattingRoom.class));
        verifyNoInteractions(chatRoomMemberRepository, basicInformationRepository, chattingRequiredAlarmRepository, alarmService);
    }

    @Test
    @DisplayName("피요청자가 채팅 요청을 수락하면 상태를 수락으로 변경하고 요청자에게 알림을 전송한다")
    void acceptRequiredChangesStatusAndPublishesAlarm() {
        // Given
        Long requestId = 1000L;
        Long requesterId = 1L;
        Long requesteeId = 2L;
        Member requester = Member.builder().id(requesterId).build();
        Member requestee = Member.builder().id(requesteeId).build();
        ChattingRequired chattingRequired = chatRequest(requester, requestee, ChattingRequiredStatus.PENDING);

        when(chattingRequiredRepository.findById(requestId)).thenReturn(Optional.of(chattingRequired));
        when(basicInformationRepository.findLatestBasicInformation(requestee))
                .thenReturn(Optional.of(basicInformation(requestee, "이수현")));
        when(chattingRequiredAlarmRepository.save(any(ChattingRequiredAlarm.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        ChatRequestDto.Response response = chatRequestService.acceptRequired(requesteeId, requestId);

        // Then
        assertThat(chattingRequired.getStatus()).isEqualTo(ChattingRequiredStatus.ACCEPTED);
        assertThat(response.getUpdatedAt()).isNotNull();
        assertChattingRequiredAlarm(requester, "이수현님이 매칭 요청을 수락했어요", chattingRequired);
        verify(alarmService).sendToClient(eq(requesterId), eq(AlarmType.CHATTING_REQUIRED.name()), any(ChattingRequiredAlarm.class));
    }

    @Test
    @DisplayName("피요청자가 채팅 요청을 거절하면 상태를 거절로 변경하고 요청자에게 알림을 전송한다")
    void rejectRequiredChangesStatusAndPublishesAlarm() {
        // Given
        Long requestId = 1000L;
        Long requesterId = 1L;
        Long requesteeId = 2L;
        Member requester = Member.builder().id(requesterId).build();
        Member requestee = Member.builder().id(requesteeId).build();
        ChattingRequired chattingRequired = chatRequest(requester, requestee, ChattingRequiredStatus.PENDING);

        when(chattingRequiredRepository.findById(requestId)).thenReturn(Optional.of(chattingRequired));
        when(basicInformationRepository.findLatestBasicInformation(requestee))
                .thenReturn(Optional.of(basicInformation(requestee, "이수현")));
        when(chattingRequiredAlarmRepository.save(any(ChattingRequiredAlarm.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        ChatRequestDto.Response response = chatRequestService.rejectRequired(requesteeId, requestId);

        // Then
        assertThat(chattingRequired.getStatus()).isEqualTo(ChattingRequiredStatus.REJECTED);
        assertThat(response.getUpdatedAt()).isNotNull();
        assertChattingRequiredAlarm(requester, "이수현님이 매칭 요청을 거절했어요", chattingRequired);
        verify(alarmService).sendToClient(eq(requesterId), eq(AlarmType.CHATTING_REQUIRED.name()), any(ChattingRequiredAlarm.class));
    }

    @Test
    @DisplayName("요청자가 채팅 요청을 취소하면 상태를 취소로 변경하고 피요청자에게 알림을 전송한다")
    void cancelRequiredChangesStatusAndPublishesAlarm() {
        // Given
        Long requestId = 1000L;
        Long requesterId = 1L;
        Long requesteeId = 2L;
        Member requester = Member.builder().id(requesterId).build();
        Member requestee = Member.builder().id(requesteeId).build();
        ChattingRequired chattingRequired = chatRequest(requester, requestee, ChattingRequiredStatus.PENDING);

        when(chattingRequiredRepository.findById(requestId)).thenReturn(Optional.of(chattingRequired));
        when(basicInformationRepository.findLatestBasicInformation(requester))
                .thenReturn(Optional.of(basicInformation(requester, "김중민")));
        when(chattingRequiredAlarmRepository.save(any(ChattingRequiredAlarm.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        ChatRequestDto.Response response = chatRequestService.cancelRequired(requesterId, requestId);

        // Then
        assertThat(chattingRequired.getStatus()).isEqualTo(ChattingRequiredStatus.CANCELED);
        assertThat(response.getUpdatedAt()).isNotNull();
        assertChattingRequiredAlarm(requestee, "김중민님이 매칭 요청을 취소했어요", chattingRequired);
        verify(alarmService).sendToClient(eq(requesteeId), eq(AlarmType.CHATTING_REQUIRED.name()), any(ChattingRequiredAlarm.class));
    }

    @Test
    @DisplayName("요청자가 채팅 요청 수락을 시도하면 권한 없음 예외를 던지고 알림을 전송하지 않는다")
    void acceptRequiredRejectsRequesterAccess() {
        // Given
        Long requestId = 1000L;
        Long requesterId = 1L;
        ChattingRequired chattingRequired = chatRequest(
                Member.builder().id(requesterId).build(),
                Member.builder().id(2L).build(),
                ChattingRequiredStatus.PENDING
        );

        when(chattingRequiredRepository.findById(requestId)).thenReturn(Optional.of(chattingRequired));

        // When & Then
        assertThatThrownBy(() -> chatRequestService.acceptRequired(requesterId, requestId))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getErrorCode()).isEqualTo(RequiredErrorCode.CHATTING_ACCESS_DENIED));
        assertThat(chattingRequired.getStatus()).isEqualTo(ChattingRequiredStatus.PENDING);
        verifyNoInteractions(basicInformationRepository, chattingRequiredAlarmRepository, alarmService);
    }

    @Test
    @DisplayName("존재하지 않는 채팅 요청을 거절하려 하면 조회 실패 예외를 던지고 알림을 전송하지 않는다")
    void rejectRequiredRejectsMissingRequest() {
        // Given
        Long requestId = 1000L;
        when(chattingRequiredRepository.findById(requestId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> chatRequestService.rejectRequired(2L, requestId))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getErrorCode()).isEqualTo(RequiredErrorCode.CHATTING_NOT_FOUND));
        verifyNoInteractions(basicInformationRepository, chattingRequiredAlarmRepository, alarmService);
    }

    @Test
    @DisplayName("이미 처리된 채팅 요청을 취소하려 하면 상태 오류 예외를 던지고 알림을 전송하지 않는다")
    void cancelRequiredRejectsInvalidStatus() {
        // Given
        Long requestId = 1000L;
        Long requesterId = 1L;
        ChattingRequired chattingRequired = chatRequest(
                Member.builder().id(requesterId).build(),
                Member.builder().id(2L).build(),
                ChattingRequiredStatus.ACCEPTED
        );

        when(chattingRequiredRepository.findById(requestId)).thenReturn(Optional.of(chattingRequired));

        // When & Then
        assertThatThrownBy(() -> chatRequestService.cancelRequired(requesterId, requestId))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getErrorCode()).isEqualTo(RequiredErrorCode.CHATTING_INVALID_STATUS));
        assertThat(chattingRequired.getStatus()).isEqualTo(ChattingRequiredStatus.ACCEPTED);
        verifyNoInteractions(basicInformationRepository, chattingRequiredAlarmRepository, alarmService);
    }

    private ChatRequestDto.Request chatRequest(Long requesteeId, Long boardId) {
        ChatRequestDto.Request request = new ChatRequestDto.Request();
        request.setRequesteeId(requesteeId);
        request.setBoardId(boardId);
        return request;
    }

    private ChattingRequired chatRequest(Member requester, Member requestee, ChattingRequiredStatus status) {
        return ChattingRequired.builder()
                .requester(requester)
                .requestee(requestee)
                .status(status)
                .build();
    }

    private BasicInformation basicInformation(Member member, String name) {
        return BasicInformation.builder()
                .member(member)
                .name(name)
                .birth(LocalDate.of(1998, 1, 1))
                .gender(Gender.MALE)
                .email(name + "@example.com")
                .build();
    }

    private void assertChattingRequiredAlarm(Member receiver, String message, ChattingRequired chattingRequired) {
        ArgumentCaptor<ChattingRequiredAlarm> alarmCaptor = ArgumentCaptor.forClass(ChattingRequiredAlarm.class);
        verify(chattingRequiredAlarmRepository).save(alarmCaptor.capture());
        assertThat(alarmCaptor.getValue().getMember()).isSameAs(receiver);
        assertThat(alarmCaptor.getValue().getTitle()).isEqualTo(message);
        assertThat(alarmCaptor.getValue().getContents()).isEqualTo(message);
        assertThat(alarmCaptor.getValue().getType()).isEqualTo(AlarmType.CHATTING_REQUIRED);
        assertThat(alarmCaptor.getValue().getChattingRequired()).isSameAs(chattingRequired);
    }
}
