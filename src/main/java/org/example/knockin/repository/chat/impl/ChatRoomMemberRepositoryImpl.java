package org.example.knockin.repository.chat.impl;

import static org.example.knockin.entity.chat.QChatRoomMember.chatRoomMember;

import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.example.knockin.entity.chat.ChatRoomMember;
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

    @Override
    public Optional<ChatRoomMember> findActiveMemberByRoomIdAndMemberId(Long chatRoomId, Long memberId) {
        return Optional.ofNullable(jpaQueryFactory
                .select(chatRoomMember)
                .from(chatRoomMember)
                .where(
                        chatRoomMember.chattingRoom.id.eq(chatRoomId),
                        chatRoomMember.member.id.eq(memberId),
                        chatRoomMember.isLeft.isFalse())
                .fetchFirst()
        );
    }
}
