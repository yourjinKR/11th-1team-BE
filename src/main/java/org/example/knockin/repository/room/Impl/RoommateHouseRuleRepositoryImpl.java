package org.example.knockin.repository.room.Impl;

import static org.example.knockin.entity.room.QRoommateHouseRule.roommateHouseRule;
import static org.example.knockin.entity.room.QMyRoommate.myRoommate;
import static org.example.knockin.entity.room.QRoommateMatchingRequired.roommateMatchingRequired;

import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.example.knockin.entity.room.RoommateHouseRule;
import org.example.knockin.repository.room.RoommateHouseRuleRepositoryCustom;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class RoommateHouseRuleRepositoryImpl implements RoommateHouseRuleRepositoryCustom {
    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Optional<RoommateHouseRule> findWithFetchedById(Long id) {
        return Optional.ofNullable(
                jpaQueryFactory
                        .select(roommateHouseRule)
                        .from(roommateHouseRule)
                        .join(roommateHouseRule.myRoommate, myRoommate).fetchJoin()
                        .join(myRoommate.roommateMatchingRequired, roommateMatchingRequired)
                        .where(
                                roommateHouseRule.id.eq(id),
                                roommateHouseRule.isDeleted.isFalse()
                        )
                        .fetchOne()
        );
    }
}
