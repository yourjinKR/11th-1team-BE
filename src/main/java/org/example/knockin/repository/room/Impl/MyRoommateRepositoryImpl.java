package org.example.knockin.repository.room.Impl;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.example.knockin.entity.member.Member;
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
                .where(chattingRequired.requestee.eq(member).or(chattingRequired.requester.eq(member)))
                .fetchFirst();

        return fetchOne != null;
    }
}