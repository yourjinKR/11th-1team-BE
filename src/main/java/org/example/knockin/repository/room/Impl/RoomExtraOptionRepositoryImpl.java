package org.example.knockin.repository.room.Impl;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.example.knockin.repository.room.RoomExtraOptionRepositoryCustom;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class RoomExtraOptionRepositoryImpl implements RoomExtraOptionRepositoryCustom {
    private final JPAQueryFactory jpaQueryFactory;
}
