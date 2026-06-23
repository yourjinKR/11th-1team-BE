package org.example.knockin.repository.chat.impl;

import static org.example.knockin.entity.chat.QChattingRoom.chattingRoom;
import static org.example.knockin.entity.chat.QChatRoomMember.chatRoomMember;
import static org.example.knockin.entity.member.QMember.member;

import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.example.knockin.entity.chat.ChatRoomMember;
import org.example.knockin.entity.chat.ChattingRoom;
import org.example.knockin.entity.member.Member;
import org.example.knockin.repository.chat.ChatRoomMemberRepositoryCustom;
import org.jspecify.annotations.NullMarked;
import org.springframework.stereotype.Repository;

@NullMarked
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

    @Override
    public Member findPartnerMember(ChatRoomMember me, ChattingRoom chattingRoomEntity) {
        return Objects.requireNonNull(jpaQueryFactory
                .select(member)
                .from(chatRoomMember)
                .join(chatRoomMember.chattingRoom, chattingRoom)
                .join(chatRoomMember.member, member)
                .where(
                        chatRoomMember.chattingRoom.eq(chattingRoomEntity),
                        chatRoomMember.ne(me)
                )
                .fetchFirst());
    }

    @Override
    public Member findPartnerMember(ChatRoomMember me, Long chattingRoomId) {
        return Objects.requireNonNull(jpaQueryFactory
                .select(member)
                .from(chatRoomMember)
                .join(chatRoomMember.chattingRoom, chattingRoom)
                .join(chatRoomMember.member, member)
                .where(
                        chatRoomMember.chattingRoom.id.eq(chattingRoomId),
                        chatRoomMember.ne(me)
                )
                .fetchFirst());
    }
}
