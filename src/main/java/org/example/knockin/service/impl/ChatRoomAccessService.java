package org.example.knockin.service.impl;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.example.knockin.entity.chat.ChatRoomMember;
import org.example.knockin.global.exception.BusinessException;
import org.example.knockin.global.exception.ChattingErrorCode;
import org.example.knockin.repository.chat.ChatRoomMemberRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatRoomAccessService {

    private final ChatRoomMemberRepository chatRoomMemberRepository;

    public void checkCanSubscribe(Long chatRoomId, Long memberId) {
        if (!chatRoomMemberRepository.existsActiveMember(chatRoomId, memberId)) {
            throw new BusinessException(ChattingErrorCode.ROOM_ACCESS_DENIED);
        }
    }

    public void checkCanSendMessage(Long chatRoomId, Long memberId) {
        if (!chatRoomMemberRepository.existsActiveMember(chatRoomId, memberId)) {
            throw new BusinessException(ChattingErrorCode.ROOM_MEMBER_NOT_FOUND);
        }
    }
}
