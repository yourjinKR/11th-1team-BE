package org.example.knockin.repository.member.impl;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.example.knockin.repository.member.BasicInformationRepositoryCustom;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class BasicInformationRepositoryImpl implements BasicInformationRepositoryCustom {
    private final JPAQueryFactory jpaQueryFactory;
}
