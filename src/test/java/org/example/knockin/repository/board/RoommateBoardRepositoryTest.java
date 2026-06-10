package org.example.knockin.repository.board;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.List;
import jakarta.persistence.EntityManager;
import org.example.knockin.config.QueryDslConfig;
import org.example.knockin.dto.BoardDetailDto;
import org.example.knockin.entity.auth.Authentication;
import org.example.knockin.entity.auth.AuthenticationType;
import org.example.knockin.entity.auth.LoginProviderType;
import org.example.knockin.entity.board.RoommateBoard;
import org.example.knockin.entity.board.RoommateBoardFile;
import org.example.knockin.entity.board.RoommateBoardOption;
import org.example.knockin.entity.file.BasicInformationFile;
import org.example.knockin.entity.file.File;
import org.example.knockin.entity.file.FileType;
import org.example.knockin.entity.life.LifePattern;
import org.example.knockin.entity.life.LifePatternInformation;
import org.example.knockin.entity.life.LifePatternType;
import org.example.knockin.entity.life.MemberLifePattern;
import org.example.knockin.entity.life.PreferenceCondition;
import org.example.knockin.entity.member.BasicInformation;
import org.example.knockin.entity.member.Gender;
import org.example.knockin.entity.member.Member;
import org.example.knockin.entity.member.MemberRole;
import org.example.knockin.entity.room.Region;
import org.example.knockin.entity.room.RoomExtraOption;
import org.example.knockin.entity.room.RoomType;
import org.example.knockin.global.exception.BusinessException;
import org.example.knockin.global.exception.RoommateBoardErrorCode;
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
@DisplayName("룸메이트 게시글 Repository")
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

    @Test
    @DisplayName("상세 조회는 게시글 상세 화면에 필요한 정보를 조립한다")
    void viewDetailReturnsBoardDetail() {
        // Given
        Member member = persistMember("provider-detail");
        LocalDate birth = LocalDate.of(1998, 1, 1);
        BasicInformation basicInformation = persistBasicInformation(member, "상세작성자", Gender.FEMALE, "detail@example.com", birth);
        File profileFile = persistFile("profile.png");
        persistBasicInformationFile(basicInformation, profileFile);

        Region city = persistRegion("서울", 1, null);
        Region district = persistRegion("강남구", 2, city);
        Region dong = persistRegion("역삼동", 3, district);
        RoomType roomType = persistRoomType("원룸");
        RoommateBoard board = persistBoard("상세 게시글", member, roomType, dong, LocalDateTime.of(2026, 7, 1, 9, 0));

        for (int i = 1; i <= 11; i++) {
            persistBoardFile(board, persistFile("room-" + i + ".jpg"), false);
        }
        RoommateBoardFile thumbnailBoardFile = persistBoardFile(board, persistFile("thumbnail.jpg"), true);

        RoomExtraOption fullOption = persistRoomExtraOption("풀옵션");
        persistRoommateBoardOption(board, fullOption);

        LifePattern sleepPattern = persistLifePattern("취침", LifePatternType.SCALE);
        persistMemberLifePattern(member, persistLifePatternInformation(sleepPattern, "23:00", "일찍 자요"));
        LifePattern visitorPattern = persistLifePattern("방문객", LifePatternType.SINGLE_CHOICE);
        persistMemberLifePattern(member, persistLifePatternInformation(visitorPattern, "가끔", "가끔 방문해요"));
        LifePattern smokingPattern = persistLifePattern("흡연", LifePatternType.BOOLEAN);
        persistPreferenceCondition(member, persistLifePatternInformation(smokingPattern, "비흡연", "비흡연 선호"));

        persistAuthentication(member, AuthenticationType.STUDENT);
        entityManager.flush();
        entityManager.clear();

        // When
        BoardDetailDto.Response response = roommateBoardRepository.viewDetail(board.getId());

        // Then
        assertThat(response.getBoardId()).isEqualTo(board.getId());
        assertThat(response.getTitle()).isEqualTo("상세 게시글");
        assertThat(response.getContents()).isEqualTo("테스트 게시글 내용");
        assertThat(response.getHits()).isZero();
        assertThat(response.getRoomTypeName()).isEqualTo("원룸");
        assertThat(response.getRegionFullName()).isEqualTo("서울 강남구 역삼동");
        assertThat(response.getMemberName()).isEqualTo("상세작성자");
        assertThat(response.getMemberProfileImageUrl()).isEqualTo("profile.png");
        assertThat(response.getMemberAge()).isEqualTo(Period.between(birth, LocalDate.now()).getYears());
        assertThat(response.getGender()).isEqualTo(Gender.FEMALE);
        assertThat(response.getAuthentications()).containsExactly(AuthenticationType.STUDENT);
        assertThat(response.getRoomExtraOptionNames()).containsExactly("풀옵션");
        assertThat(response.getImages()).hasSize(10);
        assertThat(response.getImages().getFirst().getBoardFileId()).isEqualTo(thumbnailBoardFile.getId());
        assertThat(response.getImages().getFirst().getUrl()).isEqualTo("thumbnail.jpg");
        assertThat(response.getPrimaryLifeStyles())
                .extracting(BoardDetailDto.Response.Lifestyle::getName)
                .containsExactly("취침");
        assertThat(response.getAdditionalLifeStyles())
                .extracting(BoardDetailDto.Response.Lifestyle::getName)
                .containsExactly("방문객");
        assertThat(response.getConditions())
                .extracting(BoardDetailDto.Response.Condition::getName)
                .containsExactly("흡연");
    }

    @Test
    @DisplayName("조회수 증가 쿼리는 삭제되지 않은 게시글의 조회수를 1 증가시킨다")
    void increaseHitsByIdIncrementsVisibleBoardHits() {
        // Given
        Member member = persistMember("provider-hit-up");
        RoomType roomType = persistRoomType("투룸");
        Region region = persistRegion("방배동", 3, null);
        RoommateBoard board = persistBoard("조회수 증가 게시글", member, roomType, region, LocalDateTime.of(2026, 7, 1, 9, 0));
        entityManager.flush();
        entityManager.clear();

        // When
        int updatedCount = roommateBoardRepository.increaseHitsById(board.getId());
        entityManager.flush();
        entityManager.clear();

        // Then
        RoommateBoard foundBoard = entityManager.find(RoommateBoard.class, board.getId());
        assertThat(updatedCount).isEqualTo(1);
        assertThat(foundBoard.getHits()).isEqualTo(1L);
    }

    @Test
    @DisplayName("조회수 증가 쿼리는 삭제된 게시글이면 변경하지 않는다")
    void increaseHitsByIdDoesNotUpdateDeletedBoard() {
        // Given
        Member member = persistMember("provider-deleted-hit-up");
        RoomType roomType = persistRoomType("원룸");
        Region region = persistRegion("논현동", 3, null);
        RoommateBoard board = persistBoard("삭제된 게시글", member, roomType, region, LocalDateTime.of(2026, 7, 1, 9, 0));
        ReflectionTestUtils.setField(board, "isDeleted", true);
        entityManager.flush();
        entityManager.clear();

        // When
        int updatedCount = roommateBoardRepository.increaseHitsById(board.getId());
        entityManager.flush();
        entityManager.clear();

        // Then
        RoommateBoard foundBoard = entityManager.find(RoommateBoard.class, board.getId());
        assertThat(updatedCount).isZero();
        assertThat(foundBoard.getHits()).isZero();
    }

    @Test
    @DisplayName("상세 조회 대상 게시글이 없으면 조회 실패 예외를 던진다")
    void viewDetailThrowsWhenBoardDoesNotExist() {
        assertThatThrownBy(() -> roommateBoardRepository.viewDetail(999L))
                .isInstanceOfSatisfying(BusinessException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(RoommateBoardErrorCode.ROOMMATE_BOARD_NOT_FOUND));
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
        return persistBasicInformation(member, name, gender, email, LocalDate.of(1998, 1, 1));
    }

    private BasicInformation persistBasicInformation(Member member, String name, Gender gender, String email, LocalDate birth) {
        BasicInformation basicInformation = BasicInformation.builder()
                .member(member)
                .name(name)
                .birth(birth)
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

    private File persistFile(String savedFileName) {
        File file = File.builder()
                .type(FileType.ROOMMATE_BOARD_IMAGE)
                .originalFileName(savedFileName)
                .savedFileName(savedFileName)
                .fileExt("jpg")
                .build();
        entityManager.persist(file);
        return file;
    }

    private BasicInformationFile persistBasicInformationFile(BasicInformation basicInformation, File file) {
        BasicInformationFile basicInformationFile = newInstance(BasicInformationFile.class);
        ReflectionTestUtils.setField(basicInformationFile, "basicInformation", basicInformation);
        ReflectionTestUtils.setField(basicInformationFile, "file", file);
        entityManager.persist(basicInformationFile);
        return basicInformationFile;
    }

    private RoommateBoardFile persistBoardFile(RoommateBoard board, File file, boolean isThumbnail) {
        RoommateBoardFile roommateBoardFile = RoommateBoardFile.builder()
                .roommateBoard(board)
                .file(file)
                .isThumbnail(isThumbnail)
                .build();
        entityManager.persist(roommateBoardFile);
        return roommateBoardFile;
    }

    private RoomExtraOption persistRoomExtraOption(String name) {
        RoomExtraOption option = newInstance(RoomExtraOption.class);
        ReflectionTestUtils.setField(option, "name", name);
        ReflectionTestUtils.setField(option, "isDeleted", false);
        entityManager.persist(option);
        return option;
    }

    private RoommateBoardOption persistRoommateBoardOption(RoommateBoard board, RoomExtraOption option) {
        RoommateBoardOption roommateBoardOption = newInstance(RoommateBoardOption.class);
        ReflectionTestUtils.setField(roommateBoardOption, "roommateBoard", board);
        ReflectionTestUtils.setField(roommateBoardOption, "roomExtraOption", option);
        entityManager.persist(roommateBoardOption);
        return roommateBoardOption;
    }

    private LifePattern persistLifePattern(String name, LifePatternType type) {
        LifePattern pattern = newInstance(LifePattern.class);
        ReflectionTestUtils.setField(pattern, "name", name);
        ReflectionTestUtils.setField(pattern, "dtype", type);
        ReflectionTestUtils.setField(pattern, "isDeleted", false);
        entityManager.persist(pattern);
        return pattern;
    }

    private LifePatternInformation persistLifePatternInformation(LifePattern pattern, String value, String description) {
        LifePatternInformation information = newInstance(LifePatternInformation.class);
        ReflectionTestUtils.setField(information, "lifePattern", pattern);
        ReflectionTestUtils.setField(information, "dvalue", value);
        ReflectionTestUtils.setField(information, "description", description);
        entityManager.persist(information);
        return information;
    }

    private MemberLifePattern persistMemberLifePattern(Member member, LifePatternInformation information) {
        MemberLifePattern memberLifePattern = MemberLifePattern.builder()
                .member(member)
                .lifePatternInformation(information)
                .build();
        entityManager.persist(memberLifePattern);
        return memberLifePattern;
    }

    private PreferenceCondition persistPreferenceCondition(Member member, LifePatternInformation information) {
        PreferenceCondition preferenceCondition = newInstance(PreferenceCondition.class);
        ReflectionTestUtils.setField(preferenceCondition, "member", member);
        ReflectionTestUtils.setField(preferenceCondition, "lifePatternInformation", information);
        entityManager.persist(preferenceCondition);
        return preferenceCondition;
    }

    private Authentication persistAuthentication(Member member, AuthenticationType type) {
        Authentication authentication = newInstance(Authentication.class);
        ReflectionTestUtils.setField(authentication, "member", member);
        ReflectionTestUtils.setField(authentication, "type", type);
        ReflectionTestUtils.setField(authentication, "email", "auth@example.com");
        ReflectionTestUtils.setField(authentication, "code", "123456");
        ReflectionTestUtils.setField(authentication, "isAccepted", true);
        ReflectionTestUtils.setField(authentication, "isDeleted", false);
        entityManager.persist(authentication);
        return authentication;
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
