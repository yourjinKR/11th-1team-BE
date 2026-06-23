package org.example.knockin.repository.chat;

import java.util.Optional;
import org.example.knockin.entity.chat.ChatRoomMember;
import org.example.knockin.entity.chat.ChattingRoom;
import org.example.knockin.entity.member.Member;

public interface ChatRoomMemberRepositoryCustom {
    boolean existsActiveMember(Long chatRoomId, Long memberId);

    Optional<ChatRoomMember> findActiveMemberByRoomIdAndMemberId(Long chatRoomId, Long memberId);

    Member findPartnerMember(ChatRoomMember me, ChattingRoom chattingRoom);

    Member findPartnerMember(ChatRoomMember me, Long chattingRoomId);
}
