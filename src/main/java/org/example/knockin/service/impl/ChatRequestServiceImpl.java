package org.example.knockin.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.knockin.dto.ChatRequestDto;
import org.example.knockin.entity.board.RoommateBoard;
import org.example.knockin.entity.chat.ChatRoomMember;
import org.example.knockin.entity.chat.ChattingRequired;
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
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChatRequestServiceImpl {
    private final MemberRepository memberRepository;
    private final ChattingRequiredRepository chattingRequiredRepository;
    private final RoommateBoardRepository roommateBoardRepository;
    private final ChattingRoomRepository chattingRoomRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;

    @Transactional
    public ChatRequestDto.Response saveChatRequest(Long requesterId, ChatRequestDto.Request request) {
        validateChatRequest(requesterId, request);

        Member requester = memberRepository.findById(requesterId)
                        .orElseThrow(() -> new BusinessException(MemberErrorCode.MEMBER_NOT_FOUND));

        Member requestee = memberRepository.findById(request.getRequesteeId())
                .orElseThrow(() -> new BusinessException(MemberErrorCode.MEMBER_NOT_FOUND));

        boolean exists = chattingRequiredRepository.existsBetweenMembers(requester, requestee);
        if (exists) throw new BusinessException(ChattingErrorCode.REQUIRED_DUPLICATE);

        RoommateBoard roommateBoard = findRoommateBoardNullSafety(request.getBoardId());

        ChattingRequired chattingRequired = saveChattingRequired(requester, requestee, roommateBoard);

        ChattingRoom chattingRoom = saveChattingRoom(chattingRequired);
        List<Member> members = List.of(requester, requestee);
        saveChattingRoomMembers(chattingRoom, members);

        return new ChatRequestDto.Response(LocalDateTime.now());
    }

    private void validateChatRequest(Long requesterId, ChatRequestDto.Request request) {
        if (requesterId == null || request == null || request.getRequesteeId() == null || requesterId.equals(request.getRequesteeId())) {
            throw new BusinessException(CommonErrorCode.BAD_REQUEST);
        }
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
                .isAgree(true)
                .build();

        return chattingRequiredRepository.save(chattingRequired);
    }

    private ChattingRoom saveChattingRoom(ChattingRequired chattingRequired) {
        ChattingRoom chattingRoom = ChattingRoom.builder().chattingRequired(chattingRequired).build();
        return chattingRoomRepository.save(chattingRoom);
    }

    private List<ChatRoomMember> saveChattingRoomMembers(ChattingRoom chattingRoom, List<Member> members) {
        if (members.size() > 2) {
            throw new BusinessException(ChattingErrorCode.ROOM_CAPACITY_EXCEEDED);
        }

        List<ChatRoomMember> chatRoomMembers = members.stream()
                .map(member -> ChatRoomMember.of(chattingRoom, member))
                .toList();

        return chatRoomMemberRepository.saveAll(chatRoomMembers);
    }
}
