package org.example.knockin.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import org.example.knockin.dto.ChatSocketResponse;
import org.example.knockin.dto.EventType;
import org.example.knockin.dto.RoommateRequestDto;
import org.example.knockin.dto.RoommateRequestDto.RoommateMatchingRequiredInfo;
import org.example.knockin.entity.alarm.AlarmType;
import org.example.knockin.entity.chat.ChatRoomMember;
import org.example.knockin.entity.chat.ChattingRoom;
import org.example.knockin.entity.member.BasicInformation;
import org.example.knockin.entity.member.Gender;
import org.example.knockin.entity.member.Member;
import org.example.knockin.entity.room.RoommateMatchingRequired;
import org.example.knockin.entity.room.RoommateMatchingRequiredAlarm;
import org.example.knockin.entity.room.RoommateRequiredStatus;
import org.example.knockin.global.exception.BusinessException;
import org.example.knockin.global.exception.MemberErrorCode;
import org.example.knockin.global.exception.RoommateMatchingRequiredErrorCode;
import org.example.knockin.repository.chat.ChatRoomMemberRepository;
import org.example.knockin.repository.member.BasicInformationRepository;
import org.example.knockin.repository.room.RoommateMatchingRequiredAlarmRepository;
import org.example.knockin.repository.room.RoommateMatchingRequiredRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
@DisplayName("룸메이트 확정 요청 서비스")
class RoommateRequestServiceImplTest {

    @Mock
    private SimpMessageSendingOperations messagingTemplate;

    @Mock
    private RoommateMatchingRequiredRepository roommateMatchingRequiredRepository;

    @Mock
    private ChatRoomMemberRepository chatRoomMemberRepository;

    @Mock
    private RoommateMatchingRequiredAlarmRepository roommateMatchingRequiredAlarmRepository;

    @Mock
    private AlarmServiceImpl alarmService;

    @Mock
    private BasicInformationRepository basicInformationRepository;

    @InjectMocks
    private RoommateRequestServiceImpl roommateRequestService;

    @Test
    @DisplayName("대기 중인 요청이 없으면 룸메이트 확정 요청을 저장하고 알림과 채팅방 소켓 이벤트를 발행한다")
    void saveRoommateRequestCreatesRequestAndPublishesAlarmAndSocketEvent() {
        // Given
        Long chatRoomId = 10L;
        Long requesterId = 1L;
        Long requesteeId = 2L;
        Member requester = member(requesterId);
        Member requestee = member(requesteeId);
        ChattingRoom chattingRoom = chattingRoom(100L);
        ChatRoomMember roomMember = roomMember(requester, chattingRoom);
        BasicInformation requesterBasicInformation = basicInformation(requester, "김중민");

        when(chatRoomMemberRepository.findActiveMemberByRoomIdAndMemberId(chatRoomId, requesterId))
                .thenReturn(Optional.of(roomMember));
        when(chatRoomMemberRepository.findPartnerMember(roomMember, chattingRoom)).thenReturn(requestee);
        when(roommateMatchingRequiredRepository.findLatest(chatRoomId)).thenReturn(Optional.empty());
        when(roommateMatchingRequiredRepository.save(any(RoommateMatchingRequired.class)))
                .thenAnswer(invocation -> persistedRoommateRequest(invocation.getArgument(0), 1000L));
        when(basicInformationRepository.findLatestBasicInformation(requester))
                .thenReturn(Optional.of(requesterBasicInformation));
        when(roommateMatchingRequiredAlarmRepository.save(any(RoommateMatchingRequiredAlarm.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        RoommateRequestDto.Request request = request(chatRoomId);

        // When
        RoommateRequestDto.Response response = roommateRequestService.saveRoommateRequest(requesterId, request);

        // Then
        RoommateMatchingRequiredInfo info = response.getRoommateMatchingRequiredInfo();
        assertThat(info.getId()).isEqualTo(1000L);
        assertThat(info.getRequesterMemberId()).isEqualTo(requesterId);
        assertThat(info.getRequesteeMemberId()).isEqualTo(requesteeId);
        assertThat(info.getStatus()).isEqualTo(RoommateRequiredStatus.PENDING);
        assertThat(info.getCreatedAt()).isEqualTo(LocalDateTime.of(2026, 6, 23, 10, 0));
        assertThat(info.getUpdatedAt()).isEqualTo(LocalDateTime.of(2026, 6, 23, 10, 1));

        ArgumentCaptor<RoommateMatchingRequired> requestCaptor = ArgumentCaptor.forClass(RoommateMatchingRequired.class);
        verify(roommateMatchingRequiredRepository).save(requestCaptor.capture());
        assertThat(requestCaptor.getValue().getRequester()).isSameAs(requester);
        assertThat(requestCaptor.getValue().getRequestee()).isSameAs(requestee);
        assertThat(requestCaptor.getValue().getChattingRoom()).isSameAs(chattingRoom);
        assertThat(requestCaptor.getValue().getStatus()).isEqualTo(RoommateRequiredStatus.PENDING);

        ArgumentCaptor<RoommateMatchingRequiredAlarm> alarmCaptor = ArgumentCaptor.forClass(RoommateMatchingRequiredAlarm.class);
        verify(roommateMatchingRequiredAlarmRepository).save(alarmCaptor.capture());
        assertThat(alarmCaptor.getValue().getMember()).isSameAs(requestee);
        assertThat(alarmCaptor.getValue().getTitle()).isEqualTo("김중민님이 룸메이트 확정을 제안했어요");
        assertThat(alarmCaptor.getValue().getContents()).isEqualTo("김중민님이 룸메이트 확정을 제안했어요");
        assertThat(alarmCaptor.getValue().getType()).isEqualTo(AlarmType.OFFER);
        assertThat(alarmCaptor.getValue().getRoommateMatchingRequired().getId()).isEqualTo(1000L);
        verify(alarmService).sendToClient(eq(requesteeId), eq(AlarmType.OFFER.name()), any(RoommateMatchingRequiredAlarm.class));

        ArgumentCaptor<Object> socketCaptor = ArgumentCaptor.forClass(Object.class);
        verify(messagingTemplate).convertAndSend(eq("/sub/chats/10"), socketCaptor.capture());
        ChatSocketResponse<RoommateRequestDto.Response> socketResponse =
                (ChatSocketResponse<RoommateRequestDto.Response>) socketCaptor.getValue();
        assertThat(socketResponse.getEventType()).isEqualTo(EventType.ROOMMATE_REQUEST);
        assertThat(socketResponse.getChatRoomId()).isEqualTo(chatRoomId);
        assertThat(socketResponse.getPayload()).isSameAs(response);
        assertThat(socketResponse.getCreatedAt()).isNotNull();

        InOrder inOrder = inOrder(alarmService, messagingTemplate);
        inOrder.verify(alarmService).sendToClient(eq(requesteeId), eq(AlarmType.OFFER.name()), any(RoommateMatchingRequiredAlarm.class));
        inOrder.verify(messagingTemplate).convertAndSend(eq("/sub/chats/10"), any(ChatSocketResponse.class));
    }

    @Test
    @DisplayName("채팅방에 대기 중인 룸메이트 확정 요청이 있으면 중복 요청을 거부한다")
    void saveRoommateRequestRejectsDuplicatePendingRequest() {
        // Given
        Long chatRoomId = 10L;
        Long requesterId = 1L;
        Member requester = member(requesterId);
        Member requestee = member(2L);
        ChattingRoom chattingRoom = chattingRoom(100L);
        ChatRoomMember roomMember = roomMember(requester, chattingRoom);
        RoommateMatchingRequired previous = roommateRequest(requestee, requester, chattingRoom, RoommateRequiredStatus.PENDING);

        when(chatRoomMemberRepository.findActiveMemberByRoomIdAndMemberId(chatRoomId, requesterId))
                .thenReturn(Optional.of(roomMember));
        when(chatRoomMemberRepository.findPartnerMember(roomMember, chattingRoom)).thenReturn(requestee);
        when(roommateMatchingRequiredRepository.findLatest(chatRoomId)).thenReturn(Optional.of(previous));

        // When & Then
        assertThatThrownBy(() -> roommateRequestService.saveRoommateRequest(requesterId, request(chatRoomId)))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getErrorCode()).isEqualTo(RoommateMatchingRequiredErrorCode.DUPLICATE));

        verifyNoInteractions(basicInformationRepository, roommateMatchingRequiredAlarmRepository, alarmService, messagingTemplate);
    }

    @Test
    @DisplayName("종료된 이전 요청이 있으면 현재 요청자와 피요청자 방향으로 새 요청을 저장한다")
    void saveRoommateRequestCreatesNewRequestWithCurrentDirectionAfterFinishedRequest() {
        // Given
        Long chatRoomId = 10L;
        Long requesterId = 2L;
        Long requesteeId = 1L;
        Member previousRequester = member(1L);
        Member requester = member(requesterId);
        Member requestee = member(requesteeId);
        ChattingRoom chattingRoom = chattingRoom(100L);
        ChatRoomMember roomMember = roomMember(requester, chattingRoom);
        RoommateMatchingRequired previous = roommateRequest(previousRequester, requester, chattingRoom, RoommateRequiredStatus.REJECTED);

        when(chatRoomMemberRepository.findActiveMemberByRoomIdAndMemberId(chatRoomId, requesterId))
                .thenReturn(Optional.of(roomMember));
        when(chatRoomMemberRepository.findPartnerMember(roomMember, chattingRoom)).thenReturn(requestee);
        when(roommateMatchingRequiredRepository.findLatest(chatRoomId)).thenReturn(Optional.of(previous));
        when(roommateMatchingRequiredRepository.save(any(RoommateMatchingRequired.class)))
                .thenAnswer(invocation -> persistedRoommateRequest(invocation.getArgument(0), 1001L));
        when(basicInformationRepository.findLatestBasicInformation(requester))
                .thenReturn(Optional.of(basicInformation(requester, "이수현")));
        when(roommateMatchingRequiredAlarmRepository.save(any(RoommateMatchingRequiredAlarm.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        RoommateRequestDto.Response response = roommateRequestService.saveRoommateRequest(requesterId, request(chatRoomId));

        // Then
        ArgumentCaptor<RoommateMatchingRequired> requestCaptor = ArgumentCaptor.forClass(RoommateMatchingRequired.class);
        verify(roommateMatchingRequiredRepository).save(requestCaptor.capture());
        assertThat(requestCaptor.getValue().getRequester()).isSameAs(requester);
        assertThat(requestCaptor.getValue().getRequestee()).isSameAs(requestee);
        assertThat(response.getRoommateMatchingRequiredInfo().getRequesterMemberId()).isEqualTo(requesterId);
        assertThat(response.getRoommateMatchingRequiredInfo().getRequesteeMemberId()).isEqualTo(requesteeId);
    }

    @Test
    @DisplayName("요청자의 기본 정보를 찾지 못하면 알림과 채팅방 소켓 이벤트를 발행하지 않는다")
    void saveRoommateRequestDoesNotPublishSocketWhenRequesterBasicInformationIsMissing() {
        // Given
        Long chatRoomId = 10L;
        Long requesterId = 1L;
        Member requester = member(requesterId);
        Member requestee = member(2L);
        ChattingRoom chattingRoom = chattingRoom(100L);
        ChatRoomMember roomMember = roomMember(requester, chattingRoom);

        when(chatRoomMemberRepository.findActiveMemberByRoomIdAndMemberId(chatRoomId, requesterId))
                .thenReturn(Optional.of(roomMember));
        when(chatRoomMemberRepository.findPartnerMember(roomMember, chattingRoom)).thenReturn(requestee);
        when(roommateMatchingRequiredRepository.findLatest(chatRoomId)).thenReturn(Optional.empty());
        when(roommateMatchingRequiredRepository.save(any(RoommateMatchingRequired.class)))
                .thenAnswer(invocation -> persistedRoommateRequest(invocation.getArgument(0), 1000L));
        when(basicInformationRepository.findLatestBasicInformation(requester)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> roommateRequestService.saveRoommateRequest(requesterId, request(chatRoomId)))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getErrorCode()).isEqualTo(MemberErrorCode.BASIC_INFO_NOT_FOUND));

        verifyNoInteractions(roommateMatchingRequiredAlarmRepository, alarmService, messagingTemplate);
    }

    private RoommateRequestDto.Request request(Long chatRoomId) {
        RoommateRequestDto.Request request = new RoommateRequestDto.Request();
        request.setChatRoomId(chatRoomId);
        return request;
    }

    private Member member(Long id) {
        return Member.builder().id(id).build();
    }

    private ChattingRoom chattingRoom(Long id) {
        return ChattingRoom.builder().id(id).build();
    }

    private ChatRoomMember roomMember(Member member, ChattingRoom chattingRoom) {
        return ChatRoomMember.builder()
                .member(member)
                .chattingRoom(chattingRoom)
                .isLeft(false)
                .build();
    }

    private RoommateMatchingRequired roommateRequest(
            Member requester,
            Member requestee,
            ChattingRoom chattingRoom,
            RoommateRequiredStatus status
    ) {
        return RoommateMatchingRequired.builder()
                .requester(requester)
                .requestee(requestee)
                .chattingRoom(chattingRoom)
                .status(status)
                .build();
    }

    private RoommateMatchingRequired persistedRoommateRequest(RoommateMatchingRequired request, Long id) {
        ReflectionTestUtils.setField(request, "id", id);
        ReflectionTestUtils.setField(request, "createdAt", LocalDateTime.of(2026, 6, 23, 10, 0));
        ReflectionTestUtils.setField(request, "updatedAt", LocalDateTime.of(2026, 6, 23, 10, 1));
        return request;
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
}
