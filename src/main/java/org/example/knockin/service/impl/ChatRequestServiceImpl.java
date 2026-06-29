package org.example.knockin.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.example.knockin.dto.ChatRequestDetailDto;
import org.example.knockin.dto.ChatRequestDetailDto.Response.Lifestyle;
import org.example.knockin.dto.ChatRequestDetailDto.Response.MemberInfo;
import org.example.knockin.dto.ChatRequestDto;
import org.example.knockin.dto.ChatRequestListDto;
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
import org.example.knockin.global.util.DateUtils;
import org.example.knockin.global.util.HasMemberId;
import org.example.knockin.repository.board.RoommateBoardRepository;
import org.example.knockin.repository.chat.ChattingRequiredAlarmRepository;
import org.example.knockin.repository.chat.ChattingRequiredRepository;
import org.example.knockin.repository.chat.row.ChatRequestListRow;
import org.example.knockin.repository.life.MemberLifePatternRepository;
import org.example.knockin.repository.life.row.MatchingLifestyleRow;
import org.example.knockin.repository.member.BasicInformationRepository;
import org.example.knockin.repository.member.MemberRepository;
import org.example.knockin.repository.member.row.ChattingRoomBasicInfoRow;
import org.example.knockin.service.RoommateScoreService;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChatRequestServiceImpl {

    private final static Map<ChattingRequiredStatus, String> templateMap = Map.of(
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
    private final MemberLifePatternRepository memberLifePatternRepository;
    private final RoommateScoreService roommateScoreService;

    @Transactional(readOnly = true)
    public List<ChatRequestListDto.Response> getPendingChatRequestList(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(MemberErrorCode.MEMBER_NOT_FOUND));

        List<ChatRequestListRow> requestListRows = chattingRequiredRepository.findAllPendingByRequestee(member);
        List<Long> requesterIds = requestListRows.stream()
                .map(ChatRequestListRow::memberId)
                .distinct()
                .toList();
        Map<Long, Integer> scoresByMemberId = requesterIds.isEmpty()
                ? Map.of()
                : roommateScoreService.calculateSimpleScores(memberId, requesterIds);

        return requestListRows.stream()
                .map(row -> toResponse(row, scoresByMemberId.get(row.memberId())))
                .toList();
    }

    private ChatRequestListDto.Response toResponse(ChatRequestListRow row, Integer score) {
        return ChatRequestListDto.Response.builder()
                .requiredId(row.requiredId())
                .status(row.status())
                .memberId(row.memberId())
                .memberName(row.memberName())
                .memberAge(DateUtils.calculateAge(row.birth()))
                .gender(row.gender())
                .score(score)
                .createdAt(row.createdAt())
                .build();
    }

    @Transactional(readOnly = true)
    public ChatRequestDetailDto.Response getChatRequestDetail(Long memberId, Long requestId) {
        ChattingRequired chattingRequired = chattingRequiredRepository.findById(requestId)
                .orElseThrow(() -> new BusinessException(RequiredErrorCode.CHATTING_NOT_FOUND));

        boolean isRequester = isRequester(memberId, chattingRequired);
        Member requester = chattingRequired.getRequester();
        Member requestee = chattingRequired.getRequestee();
        Long myId = isRequester ? requester.getId() : requestee.getId();
        Long opponentId = isRequester ? requestee.getId() : requester.getId();

        List<ChattingRoomBasicInfoRow> basicInfoRows = basicInformationRepository.findChattingRoomBasicInfoRows(List.of(myId, opponentId));
        Map<Long, ChattingRoomBasicInfoRow> basicInfoRowMap = HasMemberId.toMapByMemberId(basicInfoRows);

        List<MatchingLifestyleRow> lifestyleRows = memberLifePatternRepository.findAllLifestyleByMemberIdIn(List.of(myId, opponentId));
        Map<Long, List<MatchingLifestyleRow>> lifeStyleRowMap = HasMemberId.groupingByMemberId(lifestyleRows);

        MemberInfo meInfo = toMemberInfo(myId, basicInfoRowMap.get(myId), lifeStyleRowMap.get(myId));
        MemberInfo opponentInfo = toMemberInfo(opponentId, basicInfoRowMap.get(opponentId), lifeStyleRowMap.get(opponentId));

        Integer score = roommateScoreService.calculateSimpleScore(myId, opponentId);

        return ChatRequestDetailDto.Response.builder()
                .requiredId(chattingRequired.getId())
                .status(chattingRequired.getStatus())
                .createdAt(chattingRequired.getCreatedAt())
                .score(score)
                .me(meInfo)
                .opponent(opponentInfo)
                .isRequester(isRequester)
                .build();
    }

    private ChatRequestDetailDto.Response.MemberInfo toMemberInfo(
            Long memberId,
            ChattingRoomBasicInfoRow basicInformationRow,
            List<MatchingLifestyleRow> lifestyleRows
    ) {
        if (basicInformationRow == null) {
            throw new BusinessException(MemberErrorCode.BASIC_INFO_NOT_FOUND);
        }

        List<Lifestyle> lifestyles = (lifestyleRows == null ? List.<MatchingLifestyleRow>of() : lifestyleRows)
                .stream()
                .map(this::toLifeStyle)
                .toList();

        return ChatRequestDetailDto.Response.MemberInfo.builder()
                .memberId(memberId)
                .memberName(basicInformationRow.name())
                .memberAge(DateUtils.calculateAge(basicInformationRow.birth()))
                .gender(basicInformationRow.gender())
                .profileImageUrl(basicInformationRow.profileImageUrl())
                .lifeStyles(lifestyles)
                .build();
    }

    private ChatRequestDetailDto.Response.Lifestyle toLifeStyle(MatchingLifestyleRow row) {
        return ChatRequestDetailDto.Response.Lifestyle.builder()
                .lifestyleId(row.lifestyleId())
                .name(row.name())
                .value(row.value())
                .description(row.description())
                .type(row.type())
                .build();
    }

    private boolean isRequester(Long memberId, ChattingRequired chattingRequired) {
        Long requesterId = chattingRequired.getRequester().getId();
        Long requesteeId = chattingRequired.getRequestee().getId();

        if (requesterId.equals(memberId)) {
            return true;
        }

        if (requesteeId.equals(memberId)) {
            return false;
        }

        throw new BusinessException(RequiredErrorCode.CHATTING_ACCESS_DENIED);
    }

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
