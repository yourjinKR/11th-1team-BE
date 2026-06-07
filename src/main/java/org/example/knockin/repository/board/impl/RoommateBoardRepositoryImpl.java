package org.example.knockin.repository.board.impl;

import static org.example.knockin.entity.board.QRoommateBoard.roommateBoard;
import static org.example.knockin.entity.member.QBasicInformation.basicInformation;
import static org.example.knockin.entity.member.QMember.member;
import static org.example.knockin.entity.board.QRoommateBoardFile.roommateBoardFile;
import static org.example.knockin.entity.room.QRoomType.roomType;
import static org.example.knockin.entity.room.QRegion.region;
import static org.example.knockin.entity.auth.QAuthentication.authentication;


import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.example.knockin.entity.auth.AuthenticationType;
import org.example.knockin.entity.file.QFile;
import org.example.knockin.entity.member.Gender;
import org.example.knockin.entity.member.QBasicInformation;
import org.example.knockin.entity.room.QRegion;
import org.example.knockin.global.jpa.QueryDslUtils;
import org.example.knockin.repository.board.RoommateBoardListRow;
import org.example.knockin.repository.board.RoommateBoardRepositoryCustom;
import org.example.knockin.repository.board.RoommateBoardSearchCondition;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class RoommateBoardRepositoryImpl implements RoommateBoardRepositoryCustom {
    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Page<RoommateBoardListRow> search(@NonNull RoommateBoardSearchCondition condition) {

        SearchAliases aliases = new SearchAliases(
                new QRegion("boardRegion"),
                new QRegion("parentRegion"),
                new QRegion("grandParentRegion"),
                new QBasicInformation("latestBasicInformation"),
                new QBasicInformation("maxBasicInformation")
        );

        Predicate[] searchCondition = {
                regionIn(condition.regionIds(), aliases.boardRegion(), aliases.parentRegion(), aliases.grandParentRegion()),
                roomTypeIn(condition.roomTypeIds()),
                genderEq(condition.gender(), aliases),
                depositBetween(condition.minDeposit(), condition.maxDeposit()),
                mounthRentBetween(condition.minMounthRent(), condition.maxMounthRent()),
                isNotDeleted(),
                comeableDateNotExpired(condition.endDate())
        };

        Pageable pageable = condition.pageable();
        List<Long> boardIds = fetchBoardIds(searchCondition, pageable, aliases);
        Long total = countBoards(searchCondition, aliases);

        if (boardIds.isEmpty()) {
            return new PageImpl<>(List.of(), pageable, total == null ? 0 : total);
        }

        List<BoardBaseRow> baseRows = fetchBaseRows(boardIds, aliases);
        Map<Long, BoardBaseRow> baseRowByBoardId = mapBaseRowsByBoardId(baseRows);
        Map<Long, String> thumbnailByBoardId = fetchThumbnailByBoardId(boardIds);
        Map<Long, List<AuthenticationType>> authByMemberId = fetchAuthenticationsByMemberId(baseRows);
        List<RoommateBoardListRow> rows = toRows(
                boardIds,
                baseRowByBoardId,
                thumbnailByBoardId,
                authByMemberId
        );

        return new PageImpl<>(rows, pageable, total == null ? 0 : total);
    }

    private List<Long> fetchBoardIds(Predicate[] searchCondition, Pageable pageable, SearchAliases aliases) {
        return jpaQueryFactory
                .select(roommateBoard.id)
                .distinct()
                .from(roommateBoard)
                .join(roommateBoard.region, aliases.boardRegion())
                .leftJoin(aliases.boardRegion().parent, aliases.parentRegion())
                .leftJoin(aliases.parentRegion().parent, aliases.grandParentRegion())
                .join(roommateBoard.member, member)
                .where(searchCondition)
                .orderBy(toBoardOrderSpecifiers(pageable.getSort()))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
    }

    private Long countBoards(Predicate[] searchCondition, SearchAliases aliases) {
        return jpaQueryFactory
                .select(roommateBoard.countDistinct())
                .from(roommateBoard)
                .join(roommateBoard.region, aliases.boardRegion())
                .leftJoin(aliases.boardRegion().parent, aliases.parentRegion())
                .leftJoin(aliases.parentRegion().parent, aliases.grandParentRegion())
                .join(roommateBoard.member, member)
                .where(searchCondition)
                .fetchOne();
    }

    private List<BoardBaseRow> fetchBaseRows(List<Long> boardIds, SearchAliases aliases) {
        return jpaQueryFactory
                .select(Projections.constructor(
                        BoardBaseRow.class,
                        roommateBoard.id,
                        roommateBoard.title,
                        roommateBoard.deposit,
                        roommateBoard.monthlyRent,
                        roommateBoard.managementCost,
                        roommateBoard.comeableDate,
                        roommateBoard.hits,
                        roomType.name,
                        region.name,
                        aliases.parentRegion().name,
                        aliases.grandParentRegion().name,
                        member.id,
                        basicInformation.name
                ))
                .from(roommateBoard)
                .join(roommateBoard.roomType, roomType)
                .join(roommateBoard.region, region)
                .leftJoin(region.parent, aliases.parentRegion())
                .leftJoin(aliases.parentRegion().parent, aliases.grandParentRegion())
                .join(roommateBoard.member, member)
                .leftJoin(member.basicInformations, basicInformation)
                .on(latestBasicInformationIdEq(aliases))
                .where(roommateBoard.id.in(boardIds))
                .fetch();
    }

    private Map<Long, BoardBaseRow> mapBaseRowsByBoardId(List<BoardBaseRow> baseRows) {
        return baseRows.stream()
                .collect(Collectors.toMap(
                        BoardBaseRow::boardId,
                        Function.identity(),
                        (first, second) -> first
                ));
    }

    private Map<Long, String> fetchThumbnailByBoardId(List<Long> boardIds) {
        QFile file = new QFile("file");

        List<BoardThumbnailRow> thumbnailRows = jpaQueryFactory
                .select(Projections.constructor(
                        BoardThumbnailRow.class,
                        roommateBoardFile.roommateBoard.id,
                        file.savedFileName
                ))
                .from(roommateBoardFile)
                .join(roommateBoardFile.file, file)
                .where(
                        roommateBoardFile.roommateBoard.id.in(boardIds),
                        roommateBoardFile.isThumbnail.isTrue()
                )
                .fetch();

        return thumbnailRows.stream()
                .collect(Collectors.toMap(
                        BoardThumbnailRow::boardId,
                        BoardThumbnailRow::imageUrl,
                        (first, second) -> first
                ));
    }

    private Map<Long, List<AuthenticationType>> fetchAuthenticationsByMemberId(List<BoardBaseRow> baseRows) {
        List<Long> memberIds = baseRows.stream()
                .map(BoardBaseRow::memberId)
                .distinct()
                .toList();

        List<MemberAuthRow> authRows = jpaQueryFactory
                .select(Projections.constructor(
                        MemberAuthRow.class,
                        authentication.member.id,
                        authentication.type
                ))
                .from(authentication)
                .where(authentication.member.id.in(memberIds))
                .fetch();

        return authRows.stream()
                .collect(Collectors.groupingBy(
                        MemberAuthRow::memberId,
                        Collectors.mapping(MemberAuthRow::type, Collectors.toList())
                ));
    }

    private List<RoommateBoardListRow> toRows(
            List<Long> boardIds,
            Map<Long, BoardBaseRow> baseRowByBoardId,
            Map<Long, String> thumbnailByBoardId,
            Map<Long, List<AuthenticationType>> authByMemberId
    ) {
        return boardIds.stream()
                .map(boardId -> {
                    BoardBaseRow row = baseRowByBoardId.get(boardId);

                    return new RoommateBoardListRow(
                            row.boardId(),
                            thumbnailByBoardId.get(row.boardId()),
                            row.title(),
                            row.deposit(),
                            row.monthlyRent(),
                            row.managementCost(),
                            List.of(row.roomTypeName()),
                            row.comeableDate(),
                            row.regionFullName(),
                            row.memberName(),
                            authByMemberId.getOrDefault(row.memberId(), List.of()),
                            row.hits(),
                            List.of()
                    );
                })
                .toList();
    }

    private OrderSpecifier<?>[] toBoardOrderSpecifiers(Sort sort) {
        List<OrderSpecifier<?>> orders = new ArrayList<>();

        for (Sort.Order order : sort) {
            Order direction = order.getDirection().isAscending() ? Order.ASC : Order.DESC;

            switch (order.getProperty()) {
                case "createdAt" -> orders.add(new OrderSpecifier<>(direction, roommateBoard.createdAt));
                case "hits" -> orders.add(new OrderSpecifier<>(direction, roommateBoard.hits));
                default -> throw new IllegalArgumentException();
            }
        }

        if (orders.isEmpty()) orders.add(roommateBoard.createdAt.desc());
        orders.add(roommateBoard.id.desc());
        return orders.toArray(new OrderSpecifier[0]);
    }

    private BooleanExpression latestBasicInformationIdEq(SearchAliases aliases) {
        return basicInformation.id.eq(
                JPAExpressions
                        .select(aliases.latestBasicInformation().id.max())
                        .from(aliases.latestBasicInformation())
                        .where(aliases.latestBasicInformation().member.id.eq(member.id))
        );
    }

    private BooleanExpression regionEq(Long regionId) {
        if (regionId == null) return null;
        return roommateBoard.region.id.eq(regionId);
    }

    private BooleanExpression regionIn(List<Long> regionIds) {
        if (regionIds == null || regionIds.isEmpty()) return null;
        return roommateBoard.region.id.in(regionIds);
    }

    private BooleanExpression regionIn(List<Long> regionIds, QRegion boardRegion, QRegion parentRegion, QRegion grandParentRegion) {
        if (regionIds == null || regionIds.isEmpty()) return null;
        List<Long> uniqueIds = regionIds.stream().filter(Objects::nonNull).distinct().toList();
        if (uniqueIds.isEmpty()) return null;
        return boardRegion.id.in(uniqueIds).or(parentRegion.id.in(uniqueIds)).or(grandParentRegion.id.in(uniqueIds));
    }

    private BooleanExpression roomTypeEq(Long roomTypeId) {
        if (roomTypeId == null) return null;
        return roommateBoard.roomType.id.eq(roomTypeId);
    }

    private BooleanExpression roomTypeIn(List<Long> roomTypeIds) {
        if (roomTypeIds == null || roomTypeIds.isEmpty()) return null;
        return roommateBoard.roomType.id.in(roomTypeIds);
    }

    private BooleanExpression genderEq(Gender gender, SearchAliases aliases) {
        if (gender == null) return null;
        return JPAExpressions
                .selectOne()
                .from(aliases.latestBasicInformation())
                .where(
                        aliases.latestBasicInformation().member.id.eq(member.id),
                        aliases.latestBasicInformation().gender.eq(gender),
                        aliases.latestBasicInformation().id.eq(
                                JPAExpressions
                                        .select(aliases.maxBasicInformation().id.max())
                                        .from(aliases.maxBasicInformation())
                                        .where(aliases.maxBasicInformation().member.id.eq(member.id))
                        )
                )
                .exists();
    }

    private BooleanExpression depositBetween(Integer minDeposit, Integer maxDeposit) {
        return QueryDslUtils.numberBetween(roommateBoard.deposit, minDeposit, maxDeposit);
    }

    private BooleanExpression mounthRentBetween(Integer minMounthRent, Integer maxMounthRent) {
        return QueryDslUtils.numberBetween(roommateBoard.monthlyRent, minMounthRent, maxMounthRent);
    }

    private BooleanExpression isNotDeleted() {
        return roommateBoard.isDeleted.isFalse();
    }

    private BooleanExpression comeableDateNotExpired(LocalDateTime endDate) {
        return roommateBoard.comeableDate.goe(endDate);
    }

    public record BoardBaseRow(
            Long boardId,
            String title,
            Integer deposit,
            Integer monthlyRent,
            Integer managementCost,
            LocalDateTime comeableDate,
            Long hits,
            String roomTypeName,

            String regionName,
            String parentRegionName,
            String grandParentRegionName,

            Long memberId,
            String memberName
    ) {
        public String regionFullName() {
            return Stream.of(grandParentRegionName, parentRegionName, regionName)
                    .filter(Objects::nonNull)
                    .filter(name -> !name.isBlank())
                    .collect(Collectors.joining(" "));
        }
    }

    public record BoardThumbnailRow(
            Long boardId,
            String imageUrl
    ) {
    }

    public record MemberAuthRow(
            Long memberId,
            AuthenticationType type
    ) {
    }

    private record SearchAliases(
            QRegion boardRegion,
            QRegion parentRegion,
            QRegion grandParentRegion,
            QBasicInformation latestBasicInformation,
            QBasicInformation maxBasicInformation
    ) {
    }
}
