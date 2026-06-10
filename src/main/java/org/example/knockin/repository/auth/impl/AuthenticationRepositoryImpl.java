package org.example.knockin.repository.auth.impl;

import static org.example.knockin.entity.auth.QAuthentication.authentication;
import static org.example.knockin.entity.member.QMember.member;

import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.knockin.entity.auth.AuthenticationType;
import org.example.knockin.repository.auth.AuthenticationRepositoryCustom;
import org.jspecify.annotations.NullMarked;
import org.springframework.stereotype.Repository;

@NullMarked
@Repository
@RequiredArgsConstructor
public class AuthenticationRepositoryImpl implements AuthenticationRepositoryCustom {
    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<AuthenticationType> getAcceptedAuthenticationTypeByMemberId(Long memberId) {
        return jpaQueryFactory
                .select(authentication.type)
                .from(authentication)
                .where(
                        authentication.member.id.eq(memberId),
                        authentication.isAccepted.isTrue(),
                        authentication.isDeleted.isFalse()
                )
                .join(authentication.member, member)
                .fetch();
    }
}
