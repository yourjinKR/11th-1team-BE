package org.example.knockin.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.knockin.dto.ChatSocketResponse;
import org.example.knockin.dto.EventType;
import org.example.knockin.dto.RoommateRequestDto;
import org.example.knockin.dto.RoommateRequestDto.Response;
import org.example.knockin.dto.RoommateRequestDto.RoommateMatchingRequiredInfo;
import org.example.knockin.dto.RoommateRequestListDto;
import org.example.knockin.entity.chat.ChatRoomMember;
import org.example.knockin.entity.chat.ChattingRoom;
import org.example.knockin.entity.member.Member;
import org.example.knockin.entity.member.MemberPrivacy;
import org.example.knockin.entity.member.MemberPrivacyType;
import org.example.knockin.entity.room.RoommateMatchingRequired;
import org.example.knockin.entity.room.RoommateRequiredStatus;
import org.example.knockin.exception.BusinessException;
import org.example.knockin.exception.RequiredErrorCode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RoommateRequestServiceImpl {
    private static final String REQUEST_PENDING_TEMPLATE = "%s님이 룸메이트 확정을 제안했어요";
    private static final String REQUEST_ACCEPTED_TEMPLATE = "%s님과 룸메이트가 확정되었어요";

    private final SimpMessageSendingOperations messagingTemplate;
    private final RoommateMatchingRequiredServiceImpl roommateMatchingRequiredService;
    private final ChatRoomMemberServiceImpl chatRoomMemberService;
    private final RoommateMatchingRequiredAlarmServiceImpl roommateMatchingRequiredAlarmService;
    private final MyRoomMateServiceImpl myRoomMateService;
    private final MemberPrivacyServiceImpl memberPrivacyService;

    @Transactional
    public RoommateRequestDto.Response saveRoommateRequest(Long requesterId, RoommateRequestDto.Request request) {
        Long chatRoomId = request.getChatRoomId();
        ChatRoomMember chatRoomMember = chatRoomMemberService.findActiveMemberByRoomIdAndMemberId(chatRoomId, requesterId);
        ChattingRoom chattingRoom = chatRoomMember.getChattingRoom();
        Member requester = chatRoomMember.getMember();
        Member requestee = chatRoomMemberService.findPartnerMember(chatRoomMember, chattingRoom.getId());

        RoommateMatchingRequired roommateMatchingRequired = roommateMatchingRequiredService.findLatest(chatRoomId)
                .map(previous -> {
                    if (previous.getStatus().equals(RoommateRequiredStatus.PENDING)) {
                        throw new BusinessException(RequiredErrorCode.ROOMMATE_DUPLICATE);
                    }
                    return roommateMatchingRequiredService.savePending(requester, requestee, chattingRoom);
                })
                .orElseGet(() -> roommateMatchingRequiredService.savePending(requester, requestee, chattingRoom));

        Response response = toDto(roommateMatchingRequired);
        sendAlarm(requestee, requester, roommateMatchingRequired, REQUEST_PENDING_TEMPLATE);
        sendRequestMessage(chatRoomId, response);
        return response;
    }

    private void sendAlarm(Member requestee, Member requester, RoommateMatchingRequired roommateMatchingRequired, String alarmTemplate) {
        roommateMatchingRequiredAlarmService.send(requestee, requester, roommateMatchingRequired, alarmTemplate);
    }

    private RoommateRequestDto.Response toDto(RoommateMatchingRequired roommateMatchingRequired) {
        RoommateMatchingRequiredInfo roommateMatchingRequiredInfo = RoommateMatchingRequiredInfo.builder()
                .requiredId(roommateMatchingRequired.getId())
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
        RoommateMatchingRequired roommateMatchingRequired = roommateMatchingRequiredService.findByIdOrThrow(requestId);

        if (!roommateMatchingRequired.isRequestee(memberId)) {
            throw new BusinessException(RequiredErrorCode.ROOMMATE_ACCESS_DENIED);
        }

        validateRequired(roommateMatchingRequired);
        roommateMatchingRequired.accept();
        myRoomMateService.save(roommateMatchingRequired);

        Member requester = roommateMatchingRequired.getRequester();
        Member requestee = roommateMatchingRequired.getRequestee();
        sendAlarm(requester, requestee, roommateMatchingRequired, REQUEST_ACCEPTED_TEMPLATE);

        Response response = toDto(roommateMatchingRequired);
        sendRequestMessage(roommateMatchingRequired.getChattingRoom().getId(), response);

        MemberPrivacy memberPrivacy = memberPrivacyService.findByMemberId(memberId).getFirst();
        memberPrivacy.changeState(MemberPrivacyType.PRIVATE);

        return response;
    }

    @Transactional
    public RoommateRequestDto.Response rejectRequired(Long memberId, Long requestId) {
        RoommateMatchingRequired roommateMatchingRequired = roommateMatchingRequiredService.findByIdOrThrow(requestId);

        if (!roommateMatchingRequired.isRequestee(memberId)) {
            throw new BusinessException(RequiredErrorCode.ROOMMATE_ACCESS_DENIED);
        }

        validateRequired(roommateMatchingRequired);
        roommateMatchingRequired.reject();
        Response response = toDto(roommateMatchingRequired);
        sendRequestMessage(roommateMatchingRequired.getChattingRoom().getId(), response);
        return response;
    }

    @Transactional
    public RoommateRequestDto.Response cancelRequired(Long memberId, Long requestId) {
        RoommateMatchingRequired roommateMatchingRequired = roommateMatchingRequiredService.findByIdOrThrow(requestId);

        if (!roommateMatchingRequired.isRequester(memberId)) {
            throw new BusinessException(RequiredErrorCode.ROOMMATE_ACCESS_DENIED);
        }

        validateRequired(roommateMatchingRequired);
        roommateMatchingRequired.cancel();
        Response response = toDto(roommateMatchingRequired);
        sendRequestMessage(roommateMatchingRequired.getChattingRoom().getId(), response);
        return response;
    }

    public Page<RoommateRequestListDto.Response> getRequiredList(Long memberId, Pageable pageable) {
        return roommateMatchingRequiredService.findByRequesterIdAndRequesteeId(memberId, memberId, pageable).map(this::toListDto);
    }

    private RoommateRequestListDto.Response toListDto(RoommateMatchingRequired roommateMatchingRequired) {
        return RoommateRequestListDto.Response.builder()
                .requiredId(roommateMatchingRequired.getId())
                .requesterId(roommateMatchingRequired.getRequester().getId())
                .requesteeId(roommateMatchingRequired.getRequestee().getId())
                .chatRoomId(roommateMatchingRequired.getChattingRoom().getId())
                .status(roommateMatchingRequired.getStatus())
                .createAt(roommateMatchingRequired.getCreatedAt())
                .build();
    }

    private void validateRequired(RoommateMatchingRequired roommateMatchingRequired) {
        if (!roommateMatchingRequired.isPending()) {
            throw new BusinessException(RequiredErrorCode.ROOMMATE_INVALID_STATUS);
        }
    }
}
