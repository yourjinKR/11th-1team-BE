package org.example.knockin.repository.chat.impl;

import static org.example.knockin.entity.chat.QChattingRequired.chattingRequired;

import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.example.knockin.entity.chat.ChattingRequired;
import org.example.knockin.entity.member.Member;
import org.example.knockin.repository.chat.ChattingRequiredRepositoryCustom;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ChattingRequiredRepositoryImpl implements ChattingRequiredRepositoryCustom {
    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public boolean existsBetweenMembers(Member memberA, Member memberB) {
        Integer result = jpaQueryFactory
                .selectOne()
                .from(chattingRequired)
                .where(
                        chattingRequired.requester.eq(memberA).and(chattingRequired.requestee.eq(memberB))
                                .or(chattingRequired.requester.eq(memberB).and(chattingRequired.requestee.eq(memberA)))
                )
                .fetchFirst();

        return result != null;
    }

    @Override
    public Optional<ChattingRequired> findLatest(Member memberA, Member memberB) {
        return Optional.ofNullable(jpaQueryFactory
                .selectFrom(chattingRequired)
                .where(
                        chattingRequired.requester.eq(memberA).and(chattingRequired.requestee.eq(memberB))
                                .or(chattingRequired.requester.eq(memberB).and(chattingRequired.requestee.eq(memberA)))
                )
                .orderBy(chattingRequired.id.desc())
                .fetchFirst());
    }
}
