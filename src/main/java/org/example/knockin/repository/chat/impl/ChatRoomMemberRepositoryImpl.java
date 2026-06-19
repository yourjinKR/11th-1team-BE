package org.example.knockin.repository.chat.impl;

import static org.example.knockin.entity.chat.QChatRoomMember.chatRoomMember;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.example.knockin.repository.chat.ChatRoomMemberRepositoryCustom;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ChatRoomMemberRepositoryImpl implements ChatRoomMemberRepositoryCustom {
    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public boolean existsActiveMember(Long chatRoomId, Long memberId) {
        Integer result = jpaQueryFactory
                .selectOne()
                .from(chatRoomMember)
                .where(
                        chatRoomMember.chattingRoom.id.eq(chatRoomId),
                        chatRoomMember.member.id.eq(memberId),
                        chatRoomMember.isLeft.isFalse()
                )
                .fetchFirst();

        return result != null;
    }
}
