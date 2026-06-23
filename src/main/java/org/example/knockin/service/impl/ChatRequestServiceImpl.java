package org.example.knockin.service.impl;

import java.time.LocalDateTime;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.example.knockin.dto.ChatRequestDto;
import org.example.knockin.entity.alarm.AlarmType;
import org.example.knockin.entity.board.RoommateBoard;
import org.example.knockin.entity.chat.ChattingRequired;
import org.example.knockin.entity.chat.ChattingRequiredAlarm;
import org.example.knockin.entity.chat.ChattingRequiredStatus;
import org.example.knockin.entity.member.BasicInformation;
import org.example.knockin.entity.member.Member;
import org.example.knockin.global.exception.BusinessException;
import org.example.knockin.global.exception.CommonErrorCode;
import org.example.knockin.global.exception.MemberErrorCode;
import org.example.knockin.global.exception.RequiredErrorCode;
import org.example.knockin.global.exception.RoommateBoardErrorCode;
import org.example.knockin.repository.board.RoommateBoardRepository;
import org.example.knockin.repository.chat.ChattingRequiredAlarmRepository;
import org.example.knockin.repository.chat.ChattingRequiredRepository;
import org.example.knockin.repository.member.BasicInformationRepository;
import org.example.knockin.repository.member.MemberRepository;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChatRequestServiceImpl {

    private static Map<ChattingRequiredStatus, String> templateMap = Map.of(
            ChattingRequiredStatus.PENDING, "%s님이 매칭을 요청했어요",
            ChattingRequiredStatus.ACCEPTED, "%s님이 매칭 요청을 수락했어요",
            ChattingRequiredStatus.REJECTED, "%s님이 매칭 요청을 거절했어요",
            ChattingRequiredStatus.CANCELED, "%s님이 매칭 요청을 취소했어요"
    );
    private static final Integer ALARM_EXPIRE_DAYS = 7;

    private final MemberRepository memberRepository;
    private final ChattingRequiredRepository chattingRequiredRepository;
    private final RoommateBoardRepository roommateBoardRepository;
    private final AlarmServiceImpl alarmService;
    private final ChattingRequiredAlarmRepository chattingRequiredAlarmRepository;
    private final BasicInformationRepository basicInformationRepository;

    @Transactional
    public ChatRequestDto.Response saveChatRequest(Long requesterId, ChatRequestDto.Request request) {
        validateChatRequest(requesterId, request);

        Member requester = memberRepository.findById(requesterId)
                        .orElseThrow(() -> new BusinessException(MemberErrorCode.MEMBER_NOT_FOUND));

        Member requestee = memberRepository.findById(request.getRequesteeId())
                .orElseThrow(() -> new BusinessException(MemberErrorCode.MEMBER_NOT_FOUND));

        validateDuplicatePendingRequest(requester, requestee);

        RoommateBoard roommateBoard = findRoommateBoardNullSafety(request.getBoardId());
        ChattingRequired chattingRequired = saveChattingRequired(requester, requestee, roommateBoard);
        sendAlarm(requestee, requester, chattingRequired);

        return new ChatRequestDto.Response(LocalDateTime.now());
    }

    @Transactional
    public ChatRequestDto.Response acceptRequired(Long memberId, Long requestId) {
        ChattingRequired required = chattingRequiredRepository.findById(requestId)
                .orElseThrow(() -> new BusinessException(RequiredErrorCode.CHATTING_NOT_FOUND));

        if (!memberId.equals(required.getRequestee().getId())) throw new BusinessException(RequiredErrorCode.CHATTING_ACCESS_DENIED);
        if (!required.isPending()) throw new BusinessException(RequiredErrorCode.CHATTING_INVALID_STATUS);

        required.accept();
        sendAlarm(required.getRequester(), required.getRequestee(), required);
        return new ChatRequestDto.Response(LocalDateTime.now());
    }

    @Transactional
    public ChatRequestDto.Response rejectRequired(Long memberId, Long requestId) {
        ChattingRequired required = chattingRequiredRepository.findById(requestId)
                .orElseThrow(() -> new BusinessException(RequiredErrorCode.CHATTING_NOT_FOUND));

        if (!memberId.equals(required.getRequestee().getId())) throw new BusinessException(RequiredErrorCode.CHATTING_ACCESS_DENIED);
        if (!required.isPending()) throw new BusinessException(RequiredErrorCode.CHATTING_INVALID_STATUS);

        required.reject();
        sendAlarm(required.getRequester(), required.getRequestee(), required);
        return new ChatRequestDto.Response(LocalDateTime.now());
    }

    @Transactional
    public ChatRequestDto.Response cancelRequired(Long memberId, Long requestId) {
        ChattingRequired required = chattingRequiredRepository.findById(requestId)
                .orElseThrow(() -> new BusinessException(RequiredErrorCode.CHATTING_NOT_FOUND));

        if (!memberId.equals(required.getRequester().getId())) throw new BusinessException(RequiredErrorCode.CHATTING_ACCESS_DENIED);
        if (!required.isPending()) throw new BusinessException(RequiredErrorCode.CHATTING_INVALID_STATUS);

        required.cancel();
        sendAlarm(required.getRequestee(), required.getRequester(), required);
        return new ChatRequestDto.Response(LocalDateTime.now());
    }

    private void validateChatRequest(Long requesterId, ChatRequestDto.Request request) {
        if (requesterId == null || request == null || request.getRequesteeId() == null || requesterId.equals(request.getRequesteeId())) {
            throw new BusinessException(CommonErrorCode.BAD_REQUEST);
        }
    }

    private void validateDuplicatePendingRequest(Member requester, Member requestee) {
        chattingRequiredRepository.findLatest(requester, requestee)
                .filter(ChattingRequired::isPending)
                .ifPresent(required -> {
                    throw new BusinessException(RequiredErrorCode.CHATTING_DUPLICATE);
                });
    }

    private RoommateBoard findRoommateBoardNullSafety(@Nullable Long boardId) {
        if (boardId == null) return null;

        return roommateBoardRepository.findById(boardId)
                .orElseThrow(() -> new BusinessException(RoommateBoardErrorCode.ROOMMATE_BOARD_NOT_FOUND));
    }

    private ChattingRequired saveChattingRequired(Member requester, Member requestee, @Nullable RoommateBoard roommateBoard) {
        ChattingRequired chattingRequired = ChattingRequired.builder()
                .requester(requester)
                .requestee(requestee)
                .roommateBoard(roommateBoard)
                .status(ChattingRequiredStatus.PENDING)
                .build();

        return chattingRequiredRepository.save(chattingRequired);
    }

    private void sendAlarm(Member requestee, Member requester, ChattingRequired chattingRequired) {
        BasicInformation basicInformation = basicInformationRepository.findLatestBasicInformation(requester)
                .orElseThrow(() -> new BusinessException(MemberErrorCode.BASIC_INFO_NOT_FOUND));
        String requesterName = basicInformation.getName();

        String formattedTemplate = String.format(templateMap.get(chattingRequired.getStatus()), requesterName);

        ChattingRequiredAlarm alarm = ChattingRequiredAlarm.builder()
                .member(requestee)
                .title(formattedTemplate)
                .contents(formattedTemplate)
                .isRead(false)
                .expiredAt(LocalDateTime.now().plusDays(ALARM_EXPIRE_DAYS))
                .type(AlarmType.CHATTING_REQUIRED)
                .chattingRequired(chattingRequired)
                .build();

        ChattingRequiredAlarm saved = chattingRequiredAlarmRepository.save(alarm);
        alarmService.sendToClient(requestee.getId(), AlarmType.CHATTING_REQUIRED.name(), saved);
    }
}
