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
import org.example.knockin.entity.room.RoommateMatchingRequired;
import org.example.knockin.entity.room.RoommateMatchingRequiredAlarm;
import org.example.knockin.entity.room.RoommateRequiredStatus;
import org.example.knockin.global.exception.BusinessException;
import org.example.knockin.global.exception.ChattingErrorCode;
import org.example.knockin.global.exception.MemberErrorCode;
import org.example.knockin.global.exception.RoommateMatchingRequiredErrorCode;
import org.example.knockin.repository.chat.ChatRoomMemberRepository;
import org.example.knockin.repository.member.BasicInformationRepository;
import org.example.knockin.repository.room.RoommateMatchingRequiredAlarmRepository;
import org.example.knockin.repository.room.RoommateMatchingRequiredRepository;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RoommateRequestServiceImpl {
    private static final String ALARM_TITLE_TEMPLATE = "%s님이 룸메이트 확정을 제안했어요";
    private static final Integer ALARM_EXPIRE_DAYS = 7;

    private final SimpMessageSendingOperations messagingTemplate;
    private final RoommateMatchingRequiredRepository roommateMatchingRequiredRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final RoommateMatchingRequiredAlarmRepository roommateMatchingRequiredAlarmRepository;
    private final AlarmServiceImpl alarmService;
    private final BasicInformationRepository basicInformationRepository;

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

        BasicInformation basicInformation = basicInformationRepository.findLatestBasicInformation(requester)
                .orElseThrow(() -> new BusinessException(MemberErrorCode.BASIC_INFO_NOT_FOUND));
        sendAlarm(requestee, basicInformation.getName() ,roommateMatchingRequired);

        RoommateMatchingRequiredInfo roommateMatchingRequiredInfo = RoommateMatchingRequiredInfo.builder()
                .id(roommateMatchingRequired.getId())
                .requesterMemberId(requesterId)
                .requesteeMemberId(requestee.getId())
                .status(roommateMatchingRequired.getStatus())
                .createdAt(roommateMatchingRequired.getCreatedAt())
                .updatedAt(roommateMatchingRequired.getUpdatedAt())
                .build();

        RoommateRequestDto.Response response = Response.builder()
                .roommateMatchingRequiredInfo(roommateMatchingRequiredInfo)
                .build();

        ChatSocketResponse<RoommateRequestDto.Response> socketResponse = ChatSocketResponse.of(
                EventType.ROOMMATE_REQUEST,
                chatRoomId,
                response
        );
        messagingTemplate.convertAndSend("/sub/chats/" + chatRoomId, socketResponse);
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

    private void sendAlarm(Member requestee, String requesterName, RoommateMatchingRequired roommateMatchingRequired) {
        RoommateMatchingRequiredAlarm alarm = RoommateMatchingRequiredAlarm.builder()
                .member(requestee)
                .title(String.format(ALARM_TITLE_TEMPLATE, requesterName))
                .contents(String.format(ALARM_TITLE_TEMPLATE, requesterName))
                .isRead(false)
                .expiredAt(LocalDateTime.now().plusDays(ALARM_EXPIRE_DAYS))
                .type(AlarmType.OFFER)
                .roommateMatchingRequired(roommateMatchingRequired)
                .build();

        RoommateMatchingRequiredAlarm saved = roommateMatchingRequiredAlarmRepository.save(alarm);
        alarmService.sendToClient(requestee.getId(), AlarmType.OFFER.name(), saved);
    }
}
