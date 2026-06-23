package org.example.knockin.service.impl;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.example.knockin.dto.ChatSocketResponse;
import org.example.knockin.dto.EventType;
import org.example.knockin.dto.RoommateRequestDto;
import org.example.knockin.dto.RoommateRequestDto.Response;
import org.example.knockin.dto.RoommateRequestDto.RoommateMatchingRequiredInfo;
import org.example.knockin.entity.alarm.AlarmType;
import org.example.knockin.entity.chat.ChatRoomMember;
import org.example.knockin.entity.chat.ChattingRoom;
import org.example.knockin.entity.member.BasicInformation;
import org.example.knockin.entity.member.Member;
import org.example.knockin.entity.room.MyRoommate;
import org.example.knockin.entity.room.RoommateMatchingRequired;
import org.example.knockin.entity.room.RoommateMatchingRequiredAlarm;
import org.example.knockin.entity.room.RoommateRequiredStatus;
import org.example.knockin.global.exception.BusinessException;
import org.example.knockin.global.exception.ChattingErrorCode;
import org.example.knockin.global.exception.MemberErrorCode;
import org.example.knockin.global.exception.RoommateMatchingRequiredErrorCode;
import org.example.knockin.repository.chat.ChatRoomMemberRepository;
import org.example.knockin.repository.member.BasicInformationRepository;
import org.example.knockin.repository.room.MyRoommateRepository;
import org.example.knockin.repository.room.RoommateMatchingRequiredAlarmRepository;
import org.example.knockin.repository.room.RoommateMatchingRequiredRepository;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RoommateRequestServiceImpl {
    private static final String REQUEST_PENDING_TEMPLATE = "%s님이 룸메이트 확정을 제안했어요";
    private static final String REQUEST_ACCEPTED_TEMPLATE = "%s님과 룸메이트가 확정되었어요";
    private static final Integer ALARM_EXPIRE_DAYS = 7;

    private final SimpMessageSendingOperations messagingTemplate;
    private final RoommateMatchingRequiredRepository roommateMatchingRequiredRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final RoommateMatchingRequiredAlarmRepository roommateMatchingRequiredAlarmRepository;
    private final AlarmServiceImpl alarmService;
    private final BasicInformationRepository basicInformationRepository;
    private final MyRoommateRepository myRoommateRepository;

    @Transactional
    public RoommateRequestDto.Response saveRoommateRequest(Long requesterId, RoommateRequestDto.Request request) {
        Long chatRoomId = request.getChatRoomId();
        ChatRoomMember chatRoomMember = chatRoomMemberRepository.findActiveMemberByRoomIdAndMemberId(chatRoomId, requesterId)
                .orElseThrow(() -> new BusinessException(ChattingErrorCode.ROOM_MEMBER_NOT_FOUND));
        ChattingRoom chattingRoom = chatRoomMember.getChattingRoom();
        Member requester = chatRoomMember.getMember();
        Member requestee = chatRoomMemberRepository.findPartnerMember(chatRoomMember, chattingRoom);

        RoommateMatchingRequired roommateMatchingRequired = roommateMatchingRequiredRepository.findLatest(chatRoomId)
                .map(previous -> {
                    if (previous.getStatus().equals(RoommateRequiredStatus.PENDING)) {
                        throw new BusinessException(RoommateMatchingRequiredErrorCode.DUPLICATE);
                    }
                    return saveRequired(requester, requestee, chattingRoom);
                })
                .orElseGet(() -> saveRequired(requester, requestee, chattingRoom));

        Response response = toDto(roommateMatchingRequired);
        sendAlarm(requestee, requester ,roommateMatchingRequired, REQUEST_PENDING_TEMPLATE);
        sendRequestMessage(chatRoomId, response);
        return response;
    }

    private RoommateMatchingRequired saveRequired(Member requester, Member requestee, ChattingRoom chattingRoom) {
        RoommateMatchingRequired roommateMatchingRequired = RoommateMatchingRequired.builder()
                .requester(requester)
                .requestee(requestee)
                .chattingRoom(chattingRoom)
                .status(RoommateRequiredStatus.PENDING)
                .build();

        return roommateMatchingRequiredRepository.save(roommateMatchingRequired);
    }

    private void sendAlarm(Member requestee, Member requester, RoommateMatchingRequired roommateMatchingRequired, String alarmTemplate) {
        BasicInformation basicInformation = basicInformationRepository.findLatestBasicInformation(requester)
                .orElseThrow(() -> new BusinessException(MemberErrorCode.BASIC_INFO_NOT_FOUND));
        String requesterName = basicInformation.getName();

        RoommateMatchingRequiredAlarm alarm = RoommateMatchingRequiredAlarm.builder()
                .member(requestee)
                .title(String.format(alarmTemplate, requesterName))
                .contents(String.format(alarmTemplate, requesterName))
                .isRead(false)
                .expiredAt(LocalDateTime.now().plusDays(ALARM_EXPIRE_DAYS))
                .type(AlarmType.OFFER)
                .roommateMatchingRequired(roommateMatchingRequired)
                .build();

        RoommateMatchingRequiredAlarm saved = roommateMatchingRequiredAlarmRepository.save(alarm);
        alarmService.sendToClient(requestee.getId(), AlarmType.OFFER.name(), saved);
    }

    private RoommateRequestDto.Response toDto(RoommateMatchingRequired roommateMatchingRequired) {
        RoommateMatchingRequiredInfo roommateMatchingRequiredInfo = RoommateMatchingRequiredInfo.builder()
                .id(roommateMatchingRequired.getId())
                .requesterMemberId(roommateMatchingRequired.getRequester().getId())
                .requesteeMemberId(roommateMatchingRequired.getRequestee().getId())
                .status(roommateMatchingRequired.getStatus())
                .createdAt(roommateMatchingRequired.getCreatedAt())
                .updatedAt(roommateMatchingRequired.getUpdatedAt())
                .build();

        return RoommateRequestDto.Response.builder()
                .roommateMatchingRequiredInfo(roommateMatchingRequiredInfo)
                .build();
    }

    private void sendRequestMessage(Long chatRoomId, RoommateRequestDto.Response response) {
        ChatSocketResponse<RoommateRequestDto.Response> socketResponse = ChatSocketResponse.of(
                EventType.ROOMMATE_REQUEST,
                chatRoomId,
                response
        );
        messagingTemplate.convertAndSend("/sub/chats/" + chatRoomId, socketResponse);
    }

    @Transactional
    public RoommateRequestDto.Response acceptRequired(Long memberId, Long requestId) {
        RoommateMatchingRequired roommateMatchingRequired = roommateMatchingRequiredRepository.findById(requestId)
                .orElseThrow(() -> new BusinessException(RoommateMatchingRequiredErrorCode.NOT_FOUND));

        if (!roommateMatchingRequired.isRequestee(memberId)) {
            throw new BusinessException(RoommateMatchingRequiredErrorCode.ACCESS_DENIED);
        }

        validateRequired(roommateMatchingRequired);
        roommateMatchingRequired.accept();
        saveMyRoommate(roommateMatchingRequired);

        Member requester = roommateMatchingRequired.getRequester();
        Member requestee = roommateMatchingRequired.getRequestee();
        sendAlarm(requester, requestee, roommateMatchingRequired, REQUEST_ACCEPTED_TEMPLATE);

        Response response = toDto(roommateMatchingRequired);
        sendRequestMessage(roommateMatchingRequired.getChattingRoom().getId(), response);
        return response;
    }

    public void saveMyRoommate(RoommateMatchingRequired roommateMatchingRequired) {
        MyRoommate myRoommate = MyRoommate.builder()
                .roommateMatchingRequired(roommateMatchingRequired)
                .isDeleted(false)
                .build();
        myRoommateRepository.save(myRoommate);
    }

    @Transactional
    public RoommateRequestDto.Response rejectRequired(Long memberId, Long requestId) {
        RoommateMatchingRequired roommateMatchingRequired = roommateMatchingRequiredRepository.findById(requestId)
                .orElseThrow(() -> new BusinessException(RoommateMatchingRequiredErrorCode.NOT_FOUND));

        if (!roommateMatchingRequired.isRequestee(memberId)) {
            throw new BusinessException(RoommateMatchingRequiredErrorCode.ACCESS_DENIED);
        }

        validateRequired(roommateMatchingRequired);
        roommateMatchingRequired.reject();
        Response response = toDto(roommateMatchingRequired);
        sendRequestMessage(roommateMatchingRequired.getChattingRoom().getId(), response);
        return response;
    }

    @Transactional
    public RoommateRequestDto.Response cancelRequired(Long memberId, Long requestId) {
        RoommateMatchingRequired roommateMatchingRequired = roommateMatchingRequiredRepository.findById(requestId)
                .orElseThrow(() -> new BusinessException(RoommateMatchingRequiredErrorCode.NOT_FOUND));

        if (!roommateMatchingRequired.isRequester(memberId)) {
            throw new BusinessException(RoommateMatchingRequiredErrorCode.ACCESS_DENIED);
        }

        validateRequired(roommateMatchingRequired);
        roommateMatchingRequired.cancel();
        Response response = toDto(roommateMatchingRequired);
        sendRequestMessage(roommateMatchingRequired.getChattingRoom().getId(), response);
        return response;
    }

    private void validateRequired(RoommateMatchingRequired roommateMatchingRequired) {
        if (!roommateMatchingRequired.isPending()) {
            throw new BusinessException(RoommateMatchingRequiredErrorCode.INVALID_STATUS);
        }
    }
}
