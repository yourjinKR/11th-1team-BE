package org.example.knockin.repository.board;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import jakarta.persistence.EntityManager;
import org.example.knockin.config.QueryDslConfig;
import org.example.knockin.entity.board.RoommateBoard;
import org.example.knockin.entity.member.BasicInformation;
import org.example.knockin.entity.member.Gender;
import org.example.knockin.entity.member.Member;
import org.example.knockin.entity.member.MemberRole;
import org.example.knockin.entity.auth.LoginProviderType;
import org.example.knockin.entity.room.Region;
import org.example.knockin.entity.room.RoomType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

@DataJpaTest
@ActiveProfiles("test")
@Import(QueryDslConfig.class)
@DisplayName("룸메이트 게시글 목록 조회 Repository")
class RoommateBoardRepositoryTest {

    @Autowired
    private RoommateBoardRepository roommateBoardRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("입주 가능시기가 노출 기준일 이전인 게시글은 목록에서 제외한다")
    void searchExcludesBoardsBeforeVisibleEndDate() {
        // Given
        LocalDateTime visibleEndDate = LocalDateTime.of(2026, 6, 1, 12, 0);
        Member member = persistMember("provider-visible");
        RoomType roomType = persistRoomType("원룸");
        Region region = persistRegion("서초동", 3, null);
        persistBoard("기준일 게시글", member, roomType, region, visibleEndDate);
        persistBoard("미래 게시글", member, roomType, region, visibleEndDate.plusDays(1));
        persistBoard("숨김 게시글", member, roomType, region, visibleEndDate.minusSeconds(1));
        entityManager.flush();
        entityManager.clear();

        RoommateBoardSearchCondition condition = defaultCondition(visibleEndDate, PageRequest.of(0, 20));

        // When
        Page<RoommateBoardListRow> result = roommateBoardRepository.search(condition);

        // Then
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent())
                .extracting(RoommateBoardListRow::title)
                .containsExactlyInAnyOrder("기준일 게시글", "미래 게시글")
                .doesNotContain("숨김 게시글");
    }

    @Test
    @DisplayName("조회 페이지에 게시글이 없어도 전체 게시글 수는 유지한다")
    void searchKeepsTotalElementsWhenRequestedPageIsEmpty() {
        // Given
        LocalDateTime visibleEndDate = LocalDateTime.of(2026, 6, 1, 12, 0);
        Member member = persistMember("provider-total");
        RoomType roomType = persistRoomType("투룸");
        Region region = persistRegion("역삼동", 3, null);
        persistBoard("첫 번째 게시글", member, roomType, region, visibleEndDate.plusDays(1));
        persistBoard("두 번째 게시글", member, roomType, region, visibleEndDate.plusDays(2));
        entityManager.flush();
        entityManager.clear();

        RoommateBoardSearchCondition condition = defaultCondition(visibleEndDate, PageRequest.of(1, 20));

        // When
        Page<RoommateBoardListRow> result = roommateBoardRepository.search(condition);

        // Then
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isEqualTo(2);
    }

    @Test
    @DisplayName("성별 필터는 회원의 최신 기본정보를 기준으로 게시글을 조회한다")
    void searchFiltersGenderByLatestBasicInformation() {
        // Given
        LocalDateTime visibleEndDate = LocalDateTime.of(2026, 6, 1, 12, 0);
        Member member = persistMember("provider-gender");
        persistBasicInformation(member, "이전정보", Gender.MALE, "old@example.com");
        persistBasicInformation(member, "최신정보", Gender.FEMALE, "new@example.com");
        RoomType roomType = persistRoomType("오피스텔");
        Region region = persistRegion("합정동", 3, null);
        persistBoard("최신 성별 기준 게시글", member, roomType, region, visibleEndDate.plusDays(1));
        entityManager.flush();
        entityManager.clear();

        RoommateBoardSearchCondition femaleCondition = condition(
                null,
                null,
                Gender.FEMALE,
                null,
                null,
                null,
                null,
                visibleEndDate,
                PageRequest.of(0, 20)
        );
        RoommateBoardSearchCondition maleCondition = condition(
                null,
                null,
                Gender.MALE,
                null,
                null,
                null,
                null,
                visibleEndDate,
                PageRequest.of(0, 20)
        );

        // When
        Page<RoommateBoardListRow> femaleResult = roommateBoardRepository.search(femaleCondition);
        Page<RoommateBoardListRow> maleResult = roommateBoardRepository.search(maleCondition);

        // Then
        assertThat(femaleResult.getContent())
                .extracting(RoommateBoardListRow::title)
                .containsExactly("최신 성별 기준 게시글");
        assertThat(femaleResult.getContent())
                .extracting(RoommateBoardListRow::memberName)
                .containsExactly("최신정보");
        assertThat(maleResult.getContent()).isEmpty();
    }

    private RoommateBoardSearchCondition defaultCondition(LocalDateTime endDate, PageRequest pageRequest) {
        return condition(null, null, null, null, null, null, null, endDate, pageRequest);
    }

    private RoommateBoardSearchCondition condition(
            List<Long> regionIds,
            List<Long> roomTypeIds,
            Gender gender,
            Integer minDeposit,
            Integer maxDeposit,
            Integer minMounthRent,
            Integer maxMounthRent,
            LocalDateTime endDate,
            PageRequest pageRequest
    ) {
        return new RoommateBoardSearchCondition(
                regionIds,
                roomTypeIds,
                gender,
                minDeposit,
                maxDeposit,
                minMounthRent,
                maxMounthRent,
                endDate,
                pageRequest
        );
    }

    private Member persistMember(String providerId) {
        Member member = Member.builder()
                .providerType(LoginProviderType.KAKAO)
                .providerId(providerId)
                .role(MemberRole.USER)
                .isDelete(false)
                .build();
        entityManager.persist(member);
        return member;
    }

    private BasicInformation persistBasicInformation(Member member, String name, Gender gender, String email) {
        BasicInformation basicInformation = BasicInformation.builder()
                .member(member)
                .name(name)
                .birth(LocalDate.of(1998, 1, 1))
                .gender(gender)
                .email(email)
                .build();
        entityManager.persist(basicInformation);
        return basicInformation;
    }

    private RoomType persistRoomType(String name) {
        RoomType roomType = newInstance(RoomType.class);
        ReflectionTestUtils.setField(roomType, "name", name);
        ReflectionTestUtils.setField(roomType, "isDeleted", false);
        entityManager.persist(roomType);
        return roomType;
    }

    private Region persistRegion(String name, Integer scope, Region parent) {
        Region region = newInstance(Region.class);
        ReflectionTestUtils.setField(region, "name", name);
        ReflectionTestUtils.setField(region, "scope", scope);
        ReflectionTestUtils.setField(region, "parent", parent);
        entityManager.persist(region);
        return region;
    }

    private RoommateBoard persistBoard(
            String title,
            Member member,
            RoomType roomType,
            Region region,
            LocalDateTime comeableDate
    ) {
        RoommateBoard board = RoommateBoard.builder()
                .member(member)
                .title(title)
                .contents("테스트 게시글 내용")
                .deposit(1_000)
                .monthlyRent(50)
                .managementCost(10)
                .roomType(roomType)
                .region(region)
                .comeableDate(comeableDate)
                .build();
        entityManager.persist(board);
        return board;
    }

    private <T> T newInstance(Class<T> type) {
        try {
            Constructor<T> constructor = type.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                 NoSuchMethodException e) {
            throw new IllegalStateException("테스트 엔티티 생성에 실패했습니다.", e);
        }
    }
}
