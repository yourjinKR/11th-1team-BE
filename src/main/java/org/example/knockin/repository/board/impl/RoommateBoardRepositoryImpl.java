package org.example.knockin.repository.board.impl;

import static org.example.knockin.entity.auth.QAuthentication.authentication;
import static org.example.knockin.entity.board.QRoommateBoard.roommateBoard;
import static org.example.knockin.entity.board.QRoommateBoardFile.roommateBoardFile;
import static org.example.knockin.entity.file.QBasicInformationFile.basicInformationFile;
import static org.example.knockin.entity.file.QFile.file;
import static org.example.knockin.entity.member.QBasicInformation.basicInformation;
import static org.example.knockin.entity.member.QMember.member;
import static org.example.knockin.entity.room.QRegion.region;
import static org.example.knockin.entity.room.QRoomType.roomType;

import com.querydsl.core.types.*;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.example.knockin.dto.BoBoardDetailDto;
import org.example.knockin.dto.BoBoardListDto;
import org.example.knockin.dto.MyBoardListDto;
import org.example.knockin.entity.auth.AuthenticationType;
import org.example.knockin.entity.file.QFile;
import org.example.knockin.entity.member.Gender;
import org.example.knockin.entity.member.Member;
import org.example.knockin.entity.member.QBasicInformation;
import org.example.knockin.entity.room.QRegion;
import org.example.knockin.global.util.QueryDslUtils;
import org.example.knockin.repository.board.row.BasicInfoRow;
import org.example.knockin.repository.board.RoommateBoardListRow;
import org.example.knockin.repository.board.RoommateBoardRepositoryCustom;
import org.example.knockin.repository.board.RoommateBoardSearchCondition;
import org.example.knockin.repository.board.row.MyRoommateBoardRow;
import org.example.knockin.repository.board.row.EditFormRow;
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
                .distinct()
                .from(authentication)
                .where(
                        authentication.member.id.in(memberIds),
                        authentication.isAccepted.isTrue(),
                        authentication.isDeleted.isFalse()
                )
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
                    String regionFullName = parseToRegionFullName(row.grandParentRegionName, row.parentRegionName, row.regionName);

                    return new RoommateBoardListRow(
                            row.boardId(),
                            thumbnailByBoardId.get(row.boardId()),
                            row.title(),
                            row.deposit(),
                            row.monthlyRent(),
                            row.managementCost(),
                            List.of(row.roomTypeName()),
                            row.comeableDate(),
                            regionFullName,
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
                default -> throw new IllegalArgumentException("지원하지 않는 정렬 조건입니다.");
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

    @Override
    public Optional<BasicInfoRow> getBasicInfo(Long boardId) {
        QRegion boardRegion = new QRegion("boardRegion");
        QRegion parentRegion = new QRegion("parentRegion");
        QRegion grandParentRegion = new QRegion("grandParentRegion");

        BasicInfoRow basicInfo = jpaQueryFactory
                .select(Projections.constructor(
                        BasicInfoRow.class,
                        roommateBoard.id,
                        roommateBoard.title,
                        roommateBoard.deposit,
                        roommateBoard.managementCost,
                        roommateBoard.monthlyRent,
                        roommateBoard.roomType.name,
                        boardRegion.name,
                        parentRegion.name,
                        grandParentRegion.name,
                        roommateBoard.comeableDateNegotiable,
                        roommateBoard.comeableDate,
                        roommateBoard.createdAt,
                        roommateBoard.hits,
                        roommateBoard.contents,
                        member.id,
                        basicInformation.name,
                        file.savedFileName,
                        basicInformation.birth,
                        basicInformation.gender
                ))
                .from(roommateBoard)
                .where(
                        roommateBoard.id.eq(boardId),
                        roommateBoard.isDeleted.isFalse()
                )
                .join(roommateBoard.roomType, roomType)
                .join(roommateBoard.region, boardRegion)
                .leftJoin(boardRegion.parent, parentRegion)
                .leftJoin(parentRegion.parent, grandParentRegion)
                .join(roommateBoard.member, member)
                // 1:N 관계이지만 서비스적으로 1:1 유지 가정
                .leftJoin(basicInformation)
                .on(basicInformation.id.eq(
                        JPAExpressions
                                .select(basicInformation.id.max())
                                .from(basicInformation)
                                .where(basicInformation.member.id.eq(member.id))
                ))
                // 1:N 관계이지만 서비스적으로 1:1 유지 가정
                .leftJoin(basicInformationFile)
                .on(basicInformationFile.id.eq(
                        JPAExpressions
                                .select(basicInformationFile.id.max())
                                .from(basicInformationFile)
                                .where(basicInformationFile.basicInformation.id.eq(basicInformation.id))
                ))
                .leftJoin(basicInformationFile.file, file)
                .on(file.isDeleted.isFalse())
                .fetchOne();

        return Optional.ofNullable(basicInfo);
    }

    @Override
    public Page<MyRoommateBoardRow> findMyBoardList(Pageable page, Member memberEntity) {
        List<MyRoommateBoardRow> content = jpaQueryFactory
                .select(Projections.constructor(MyRoommateBoardRow.class,
                        roommateBoard.id,
                        roommateBoard.title,
                        roommateBoard.deposit,
                        roommateBoard.monthlyRent,
                        roommateBoard.createdAt,
                        basicInformation.name,
                        file.savedFileName,
                        roomType.name,
                        roommateBoard.region
                ))
                .from(roommateBoard)
                .leftJoin(roommateBoardFile).on(roommateBoardFile.roommateBoard.eq(roommateBoard).and(roommateBoardFile.isThumbnail.isTrue()))
                .leftJoin(roommateBoardFile.file, file)
                .leftJoin(basicInformation).on(basicInformation.id.eq(JPAExpressions.select(basicInformation.id.max())
                        .from(basicInformation).where(basicInformation.member.eq(roommateBoard.member))))
                .leftJoin(roommateBoard.roomType, roomType)
                .where(roommateBoard.member.eq(memberEntity), roommateBoard.isDeleted.isFalse())
                .offset(page.getOffset()).limit(page.getPageSize()).orderBy(toBoardOrderSpecifiers(page.getSort())).fetch();

        Long total = jpaQueryFactory.select(roommateBoard.count()).from(roommateBoard)
                .where(roommateBoard.member.eq(memberEntity), roommateBoard.isDeleted.isFalse()).fetchOne();

        return new PageImpl<>(content, page, total != null ? total : 0L);
    }

    public Optional<EditFormRow> getEditRow(Long boardId) {
        QRegion boardRegion = new QRegion("boardRegion");
        QRegion parentRegion = new QRegion("parentRegion");
        QRegion grandParentRegion = new QRegion("grandParentRegion");

        return Optional.ofNullable(jpaQueryFactory
                .select(Projections.constructor(
                        EditFormRow.class,
                        roommateBoard.title,
                        roommateBoard.deposit,
                        roommateBoard.monthlyRent,
                        roommateBoard.managementCost,
                        roommateBoard.roomType.id,
                        roommateBoard.roomType.name,
                        boardRegion.id,
                        boardRegion.name,
                        parentRegion.name,
                        grandParentRegion.name,
                        roommateBoard.comeableDateNegotiable,
                        roommateBoard.comeableDate,
                        roommateBoard.contents
                ))
                .from(roommateBoard)
                .where(
                        roommateBoard.id.eq(boardId),
                        roommateBoard.isDeleted.isFalse()
                )
                .join(roommateBoard.roomType, roomType)
                .join(roommateBoard.region, boardRegion)
                .leftJoin(boardRegion.parent, parentRegion)
                .leftJoin(parentRegion.parent, grandParentRegion)
                .fetchOne());
    }

    @Override
    public List<BoBoardListDto.Response.BoardInfo> findBackOfficeBoardList(Pageable pageable) {
        QRegion parent = new QRegion("parent");
        QRegion grandParent = new QRegion("grandParent");
        return jpaQueryFactory.select(Projections.fields(BoBoardListDto.Response.BoardInfo.class,
                        roommateBoard.id,
                        roommateBoard.title,
                        basicInformation.name.as("writer"),
                        roommateBoard.comeableDate,
                        roommateBoard.isDeleted,
                        roommateBoard.createdAt,
                        roommateBoard.comeableDateNegotiable,
                        Expressions.stringTemplate("concat(coalesce({0}, ''), ' ', coalesce({1}, ''), ' ', coalesce({2}, ''))",
                                grandParent.name, parent.name, region.name).as("region")
                )).from(roommateBoard).join(roommateBoard.member, member)
                .leftJoin(basicInformation).on(basicInformation.member.eq(member))
                .leftJoin(roommateBoard.region, region)
                .leftJoin(region.parent, parent)
                .leftJoin(parent.parent, grandParent)
                .fetch();
    }

    @Override
    public BoBoardDetailDto.Response findBackOffcieBoard(Long id) {
        QRegion parent = new QRegion("parent");
        QRegion grandParent = new QRegion("grandParent");
        return jpaQueryFactory.select(Projections.fields(BoBoardDetailDto.Response.class,
                        file.savedFileName.as("thumbnailImage"),
                        roommateBoard.title,
                        basicInformation.name.as("writer"),
                        member.id.as("writerId"),
                        Expressions.stringTemplate("concat(coalesce({0}, ''), ' ', coalesce({1}, ''), ' ', coalesce({2}, ''))",
                                grandParent.name, parent.name, region.name).as("region"),
                        roommateBoard.deposit.longValue().as("deposit"),
                        roommateBoard.monthlyRent.longValue().as("monthlyRent"),
                        roommateBoard.comeableDateNegotiable,
                        roommateBoard.comeableDate,
                        roommateBoard.createdAt,
                        roommateBoard.hits,
                        roommateBoard.isDeleted
                ))
                .from(roommateBoard)
                .join(roommateBoard.member, member)
                .leftJoin(basicInformation)
                .on(basicInformation.member.eq(member))
                .leftJoin(roommateBoard.region, region)
                .leftJoin(region.parent, parent)
                .leftJoin(parent.parent, grandParent)
                .leftJoin(roommateBoardFile).on(roommateBoardFile.roommateBoard.eq(roommateBoard).and(roommateBoardFile.isThumbnail.isTrue()))
                .leftJoin(roommateBoardFile.file, file)
                .where(roommateBoard.id.eq(id))
                .fetchOne();
    }

    private BooleanExpression regionIn(List<Long> regionIds, QRegion boardRegion, QRegion parentRegion, QRegion grandParentRegion) {
        if (regionIds == null || regionIds.isEmpty()) return null;
        List<Long> uniqueIds = regionIds.stream().filter(Objects::nonNull).distinct().toList();
        if (uniqueIds.isEmpty()) return null;
        return boardRegion.id.in(uniqueIds).or(parentRegion.id.in(uniqueIds)).or(grandParentRegion.id.in(uniqueIds));
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
        if (endDate == null) return null;
        return roommateBoard.comeableDateNegotiable.isTrue()
                .or(roommateBoard.comeableDate.goe(endDate));
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
    }

    private String parseToRegionFullName(
            String grandParentRegionName,
            String parentRegionName,
            String regionName
    ) {
        return Stream.of(grandParentRegionName, parentRegionName, regionName)
                .filter(Objects::nonNull)
                .filter(name -> !name.isBlank())
                .collect(Collectors.joining(" "));
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
