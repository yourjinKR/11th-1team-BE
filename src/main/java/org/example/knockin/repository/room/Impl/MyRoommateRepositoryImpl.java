package org.example.knockin.repository.room.Impl;

import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.example.knockin.entity.member.Member;
import org.example.knockin.entity.member.QMember;
import org.example.knockin.entity.room.MyRoommate;
import org.example.knockin.repository.room.MyRoommateRepositoryCustom;
import org.springframework.stereotype.Repository;

import static org.example.knockin.entity.room.QMyRoommate.myRoommate;
import static org.example.knockin.entity.room.QRoommateMatchingRequired.roommateMatchingRequired;
import static org.example.knockin.entity.chat.QChattingRoom.chattingRoom;
import static org.example.knockin.entity.chat.QChattingRequired.chattingRequired;

@Repository
@RequiredArgsConstructor
public class MyRoommateRepositoryImpl implements MyRoommateRepositoryCustom {
    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public boolean isExistRoomMate(Member member) {
        Integer fetchOne = jpaQueryFactory.selectOne().from(myRoommate)
                .join(myRoommate.roommateMatchingRequired, roommateMatchingRequired)
                .join(roommateMatchingRequired.chattingRoom, chattingRoom)
                .join(chattingRoom.chattingRequired, chattingRequired)
                .where(
                        chattingRequired.requestee.eq(member).or(chattingRequired.requester.eq(member)),
                        myRoommate.isDeleted.isFalse()
                )
                .fetchFirst();

        return fetchOne != null;
    }

    @Override
    public Optional<MyRoommate> findWithRequiredByMemberId(Long memberId) {
        return Optional.ofNullable(
                jpaQueryFactory
                        .select(myRoommate)
                        .from(myRoommate)
                        .join(myRoommate.roommateMatchingRequired, roommateMatchingRequired).fetchJoin()
                        .where(
                                roommateMatchingRequired.requestee.id.eq(memberId)
                                        .or(roommateMatchingRequired.requester.id.eq(memberId)),
                                myRoommate.isDeleted.isFalse()
                        )
                        .fetchFirst()
        );
    }

    @Override
    public Optional<MyRoommate> findWithRequiredAndMembersByMemberId(Long memberId) {
        QMember requestee = new QMember("requestee");
        QMember requester = new QMember("requester");

        return Optional.ofNullable(
                jpaQueryFactory
                        .select(myRoommate)
                        .from(myRoommate)
                        .join(myRoommate.roommateMatchingRequired, roommateMatchingRequired).fetchJoin()
                        .leftJoin(roommateMatchingRequired.requester, requester).fetchJoin()
                        .leftJoin(roommateMatchingRequired.requestee, requestee).fetchJoin()
                        .where(
                                roommateMatchingRequired.requestee.id.eq(memberId)
                                        .or(roommateMatchingRequired.requester.id.eq(memberId)),
                                myRoommate.isDeleted.isFalse()
                        )
                        .fetchFirst()
        );
    }
}
