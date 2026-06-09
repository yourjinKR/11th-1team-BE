package org.example.knockin.repository.board.impl;

import static org.example.knockin.entity.auth.QAuthentication.authentication;
import static org.example.knockin.entity.board.QRoommateBoard.roommateBoard;
import static org.example.knockin.entity.board.QRoommateBoardFile.roommateBoardFile;
import static org.example.knockin.entity.board.QRoommateBoardOption.roommateBoardOption;
import static org.example.knockin.entity.file.QBasicInformationFile.basicInformationFile;
import static org.example.knockin.entity.file.QFile.file;
import static org.example.knockin.entity.life.QLifePattern.lifePattern;
import static org.example.knockin.entity.life.QLifePatternInformation.lifePatternInformation;
import static org.example.knockin.entity.life.QMemberLifePattern.memberLifePattern;
import static org.example.knockin.entity.life.QPreferenceCondition.preferenceCondition;
import static org.example.knockin.entity.member.QBasicInformation.basicInformation;
import static org.example.knockin.entity.member.QMember.member;
import static org.example.knockin.entity.room.QRegion.region;
import static org.example.knockin.entity.room.QRoomExtraOption.roomExtraOption;
import static org.example.knockin.entity.room.QRoomType.roomType;

import com.querydsl.core.Tuple;
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
import org.example.knockin.dto.BoardDetailDto;
import org.example.knockin.dto.BoardDetailDto.Response.ImageInfo;
import org.example.knockin.dto.BoardDetailDto.Response.Lifestyle;
import org.example.knockin.entity.auth.AuthenticationType;
import org.example.knockin.entity.file.QFile;
import org.example.knockin.entity.life.QLifePattern;
import org.example.knockin.entity.life.QLifePatternInformation;
import org.example.knockin.entity.member.Gender;
import org.example.knockin.entity.member.QBasicInformation;
import org.example.knockin.entity.room.QRegion;
import org.example.knockin.global.util.DateUtils;
import org.example.knockin.global.util.QueryDslUtils;
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
    public BoardDetailDto.Response viewDetail(@NonNull Long boardId) {
        QRegion boardRegion = new QRegion("boardRegion");
        QRegion parentRegion = new QRegion("parentRegion");
        QRegion grandParentRegion = new QRegion("grandParentRegion");
        SearchAliases searchAliases = new SearchAliases(boardRegion, parentRegion, grandParentRegion, null, null);

        increaseHitsByBoardId(boardId);

        Tuple tuple = getBoardBasicInfoTuple(boardId, searchAliases);

        List<ImageInfo> images = findImagesById(boardId);

        List<String> roomExtraOptionNames = findExtraOptionNamesById(boardId);

        assert tuple != null;
        Long memberId = tuple.get(basicInformation.member.id);
        BooleanExpression isPrimaryExpression = lifePattern.name.in("취침", "청결", "소음", "흡연");
        Map<Boolean, List<Lifestyle>> memberLifePatternMap = findLifeStylePrimaryKeyMapByMemberId(memberId,
                isPrimaryExpression);
        List<BoardDetailDto.Response.Lifestyle> primaryLifeStyles = memberLifePatternMap.get(true);
        List<BoardDetailDto.Response.Lifestyle> additionalLifeStyles = memberLifePatternMap.get(false);

        List<BoardDetailDto.Response.Condition> conditions = findPreferenceConditionsByMemberId(memberId);

        List<AuthenticationType> authenticationTypes = findAcceptedAuthenticationTypeByMemberId(memberId);

        String regionFullName = parseToRegionFullName(tuple.get(boardRegion.name), tuple.get(parentRegion.name), tuple.get(grandParentRegion.name));
        int memberAge = DateUtils.calculateAge(tuple.get(basicInformation.birth));

        return BoardDetailDto.Response.builder()
                .boardId(tuple.get(roommateBoard.id))
                .images(images)
                .title(tuple.get(roommateBoard.title))
                .deposit(tuple.get(roommateBoard.deposit))
                .managementCost(tuple.get(roommateBoard.managementCost))
                .monthlyRent(tuple.get(roommateBoard.monthlyRent))
                .roomTypeName(tuple.get(roommateBoard.roomType.name))
                .regionFullName(regionFullName)
                .createdAt(tuple.get(roommateBoard.createdAt))
                .hits(tuple.get(roommateBoard.hits))
                .roomExtraOptionNames(roomExtraOptionNames)
                .primaryLifeStyles(primaryLifeStyles)
                .additionalLifeStyles(additionalLifeStyles)
                .conditions(conditions)
                .memberName(tuple.get(basicInformation.name))
                .memberAge(memberAge)
                .gender(tuple.get(basicInformation.gender))
                .authentications(authenticationTypes)
                //TODO: 계산식 확정 후 적용
                .compatibility(null)
                .build();
    }

    private Tuple getBoardBasicInfoTuple(Long boardId, SearchAliases searchAliases) {
        QRegion boardRegion = searchAliases.boardRegion();
        QRegion parentRegion = searchAliases.parentRegion();
        QRegion grandParentRegion = searchAliases.grandParentRegion();

        return jpaQueryFactory
                .select(
                        roommateBoard.id,
                        roommateBoard.title,
                        roommateBoard.deposit,
                        roommateBoard.managementCost,
                        roommateBoard.monthlyRent,
                        roommateBoard.roomType.name,
                        boardRegion.name,
                        parentRegion.name,
                        grandParentRegion.name,
                        roommateBoard.createdAt,
                        roommateBoard.hits,
                        roommateBoard.contents,
                        basicInformation.name,
                        basicInformationFile.file.savedFileName,
                        basicInformation.birth,
                        basicInformation.gender
                )
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
                        JPAExpressions.select(file.id.max())
                                .from(file)
                                .where(file.id.eq(basicInformationFile.id))
                ))
                .fetchOne();
    }

    private List<ImageInfo> findImagesById(Long boardId) {
        return jpaQueryFactory
                .select(Projections.constructor(
                        ImageInfo.class,
                        roommateBoardFile.file.savedFileName,
                        roommateBoardFile.isThumbnail
                ))
                .from(roommateBoardFile)
                .join(roommateBoardFile.file, file)
                .where(
                        roommateBoardFile.roommateBoard.id.eq(boardId),
                        file.isDeleted.isFalse()
                )
                .fetch();
    }

    private List<String> findExtraOptionNamesById(Long boardId) {
        return jpaQueryFactory
                .select(roomExtraOption.name)
                .from(roomExtraOption)
                .join(roommateBoardOption.roomExtraOption, roomExtraOption)
                .where(
                        roommateBoardOption.roommateBoard.id.eq(boardId),
                        roomExtraOption.isDeleted.isFalse()
                )
                .fetch();
    }

    private Map<Boolean, List<Lifestyle>> findLifeStylePrimaryKeyMapByMemberId(Long memberId, BooleanExpression isPrimaryExpression) {
        List<Tuple> tuples = jpaQueryFactory
                .select(
                        lifePattern.id,
                        lifePattern.name,
                        lifePatternInformation.dvalue,
                        lifePatternInformation.description,
                        lifePattern.dtype,
                        isPrimaryExpression
                )
                .from(memberLifePattern)
                .where(memberLifePattern.member.id.eq(memberId))
                .join(memberLifePattern.lifePatternInformation, lifePatternInformation)
                .leftJoin(lifePatternInformation.lifePattern, lifePattern)
                .fetch();

        return tuples.stream()
                .collect(Collectors.partitioningBy(
                        tuple -> Boolean.TRUE.equals(tuple.get(isPrimaryExpression)),
                        Collectors.mapping(tuple -> new Lifestyle(
                                tuple.get(lifePattern.id),
                                tuple.get(lifePattern.name),
                                tuple.get(lifePatternInformation.dvalue),
                                tuple.get(lifePatternInformation.description),
                                tuple.get(lifePattern.dtype)
                        ), Collectors.toList())
                ));
    }

    private List<BoardDetailDto.Response.Condition> findPreferenceConditionsByMemberId(Long memberId) {
        return jpaQueryFactory
                .select(Projections.constructor(
                        BoardDetailDto.Response.Condition.class,
                        lifePattern.id,
                        lifePattern.name
                ))
                .from(preferenceCondition)
                .where(preferenceCondition.member.id.eq(memberId))
                .join(preferenceCondition.lifePatternInformation, lifePatternInformation)
                .leftJoin(lifePatternInformation.lifePattern, lifePattern)
                .fetch();
    }

    private List<AuthenticationType> findAcceptedAuthenticationTypeByMemberId(Long memberId) {
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

    private Long increaseHitsByBoardId(Long boardId) {
        return jpaQueryFactory
                .update(roommateBoard)
                .set(roommateBoard.hits, roommateBoard.hits.add(1))
                .where(roommateBoard.id.eq(boardId), roommateBoard.isDeleted.isFalse())
                .execute();
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
