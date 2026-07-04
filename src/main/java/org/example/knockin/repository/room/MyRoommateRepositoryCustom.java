package org.example.knockin.repository.room;

import java.util.Optional;
import org.example.knockin.entity.member.Member;
import org.example.knockin.entity.room.MyRoommate;

public interface MyRoommateRepositoryCustom {
    boolean isExistRoomMate(Member member);

    Optional<MyRoommate> findWithRequiredByMemberId(Long memberId);

    Optional<MyRoommate> findWithRequiredAndMembersByMemberId(Long memberId);
}