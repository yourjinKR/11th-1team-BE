package org.example.knockin.repository.member.impl;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.LockModeType;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.example.knockin.entity.member.MemberInterest;
import org.example.knockin.repository.member.MemberInterestRepositoryCustom;
import org.jspecify.annotations.NullMarked;
import org.springframework.stereotype.Repository;

import static org.example.knockin.entity.member.QMemberInterest.memberInterest;

@Repository
@NullMarked
@RequiredArgsConstructor
public class MemberInterestRepositoryImpl implements MemberInterestRepositoryCustom {
    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<Long> findActiveReceiverIdsBySenderIdAndReceiverIds(Long senderId, Collection<Long> receiverIds) {
        if (receiverIds.isEmpty()) {
            return List.of();
        }

        return jpaQueryFactory
                .select(memberInterest.receiver.id)
                .from(memberInterest)
                .where(
                        memberInterest.sender.id.eq(senderId),
                        memberInterest.receiver.id.in(receiverIds),
                        memberInterest.isDeleted.isFalse()
                )
                .fetch();
    }

    @Override
    public Optional<MemberInterest> findBySenderIdAndReceiverIdForUpdate(Long senderId, Long receiverId) {
        MemberInterest result = jpaQueryFactory
                .select(memberInterest)
                .from(memberInterest)
                .where(
                        memberInterest.sender.id.eq(senderId),
                        memberInterest.receiver.id.eq(receiverId))
                .setLockMode(LockModeType.PESSIMISTIC_WRITE)
                .fetchOne();

        return Optional.ofNullable(result);
    }
}
