package org.example.knockin.repository.room.Impl;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import static org.example.knockin.entity.room.QRoommateMatchingRequired.roommateMatchingRequired;

import org.example.knockin.dto.RoommateRequestDto.RoommateMatchingRequiredInfo;
import org.example.knockin.entity.chat.ChattingRoom;
import org.example.knockin.entity.room.RoommateMatchingRequired;
import org.example.knockin.repository.room.RoommateMatchingRequiredRepositoryCustom;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class RoommateMatchingRequiredRepositoryImpl implements RoommateMatchingRequiredRepositoryCustom {
    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Optional<RoommateMatchingRequired> findLatest(Long chattingRoomId) {
        return Optional.ofNullable(
                jpaQueryFactory
                        .select(roommateMatchingRequired)
                        .from(roommateMatchingRequired)
                        .where(
                                roommateMatchingRequired.chattingRoom.id.eq(chattingRoomId)
                        )
                        .orderBy(roommateMatchingRequired.id.desc())
                        .fetchFirst()
        );
    }

    @Override
    public List<RoommateMatchingRequiredInfo> findRequiredDto(ChattingRoom chattingRoomEntity) {
        return jpaQueryFactory
                .select(Projections.constructor(
                        RoommateMatchingRequiredInfo.class,
                        roommateMatchingRequired.id,
                        roommateMatchingRequired.requester.id,
                        roommateMatchingRequired.requestee.id,
                        roommateMatchingRequired.status,
                        roommateMatchingRequired.createdAt,
                        roommateMatchingRequired.updatedAt
                ))
                .from(roommateMatchingRequired)
                .where(roommateMatchingRequired.chattingRoom.eq(chattingRoomEntity))
                .fetch();
    }
}
