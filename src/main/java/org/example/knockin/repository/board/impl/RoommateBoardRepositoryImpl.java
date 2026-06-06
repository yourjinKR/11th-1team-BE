package org.example.knockin.repository.board.impl;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.knockin.entity.board.RoommateBoard;
import org.example.knockin.entity.member.Gender;
import org.example.knockin.global.jpa.QueryDslUtils;
import org.example.knockin.repository.board.RoommateBoardRepositoryCustom;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import static org.example.knockin.entity.board.QRoommateBoard.roommateBoard;
import static org.example.knockin.entity.member.QMember.member;
import static org.example.knockin.entity.member.QBasicInformation.basicInformation;

@Repository
@RequiredArgsConstructor
public class RoommateBoardRepositoryImpl implements RoommateBoardRepositoryCustom {
    private final JPAQueryFactory jpaQueryFactory;

    /*
    정렬은 최신순과 조회순을 지원한다.
    기본 정렬은 최신순이다.
    필터링을 지원한다.
    region에 대한 검색은 아래 계층까지 지원한다.
     */
    @Override
    public Page<@NonNull RoommateBoard> search(
            Long regionId,
            Long roomTypeId,
            Gender gender,
            Integer minDeposit,
            Integer maxDeposit,
            Integer minMounthRent,
            Integer maxMounthRent,
            @NonNull Pageable pageable
    ) {

        Predicate[] searchCondition = {
                regionEq(regionId),
                roomTypeEq(roomTypeId),
                genderEq(gender),
                depositBetween(minDeposit, maxMounthRent),
                mounthRentBetween(minMounthRent, maxMounthRent)
        };

        List<RoommateBoard> contents = jpaQueryFactory
                .selectFrom(roommateBoard)
                .where(searchCondition)
                .join(roommateBoard.member, member)
                .join(roommateBoard.member.basicInformations, basicInformation)
                .orderBy(toBoardOrderSpecifiers(pageable.getSort()))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = jpaQueryFactory
                .select(roommateBoard.count())
                .from(roommateBoard)
                .where(searchCondition)
                .fetchOne();

        return new PageImpl<>(contents, pageable, total == null ? 0 : total);
    }

    private OrderSpecifier<?>[] toBoardOrderSpecifiers(Sort sort) {
        List<OrderSpecifier<?>> orders = new ArrayList<>();

        for (Sort.Order order : sort) {
            Order direction = order.getDirection().isAscending() ? Order.ASC : Order.DESC;

            switch (order.getProperty()) {
                case "createdAt":
                    orders.add(new OrderSpecifier<>(direction, roommateBoard.createdAt));
                    break;
                case "hits":
                    orders.add(new OrderSpecifier<>(direction, roommateBoard.hits));
                    break;
                default:
                    throw new IllegalArgumentException();
            }
        }

        return orders.toArray(new OrderSpecifier[0]);
    }

    // TODO: 하위 계층 추적
    private BooleanExpression regionEq(Long regionId) {
        if (regionId == null) return null;
        return roommateBoard.region.id.eq(regionId);
    }

    private BooleanExpression roomTypeEq(Long roomTypeId) {
        if (roomTypeId == null) return null;
        return roommateBoard.roomType.id.eq(roomTypeId);
    }

    private BooleanExpression genderEq(Gender gender) {
        if (gender == null) return null;
        return basicInformation.gender.eq(gender);
    }

    private BooleanExpression depositBetween(Integer minDeposit, Integer maxDeposit) {
        return QueryDslUtils.numberBetween(roommateBoard.deposit, minDeposit, maxDeposit);
    }

    private BooleanExpression mounthRentBetween(Integer minMounthRent, Integer maxMounthRent) {
        return QueryDslUtils.numberBetween(roommateBoard.monthlyRent, minMounthRent, maxMounthRent);
    }
}
