package org.example.knockin.repository.chat;

import java.util.Optional;
import org.example.knockin.entity.chat.ChatRoomMember;

public interface ChatRoomMemberRepositoryCustom {
    boolean existsActiveMember(Long chatRoomId, Long memberId);

    Optional<ChatRoomMember> findActiveMemberByRoomIdAndMemberId(Long chatRoomId, Long memberId);
}
