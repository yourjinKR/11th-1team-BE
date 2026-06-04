package org.example.knockin.repository.board;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import org.example.knockin.config.QueryDslConfig;
import org.example.knockin.entity.auth.LoginProviderType;
import org.example.knockin.entity.board.RoommateBoard;
import org.example.knockin.entity.member.Member;
import org.example.knockin.entity.member.MemberRole;
import org.example.knockin.entity.room.Region;
import org.example.knockin.entity.room.RoomType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

@DataJpaTest
@ActiveProfiles("test")
@Import(QueryDslConfig.class)
@DisplayName("룸메이트 게시글 저장소")
class RoommateBoardRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private RoommateBoardRepository roommateBoardRepository;

    @Test
    @DisplayName("빌더로 생성한 게시글을 저장하면 삭제 여부와 조회수가 기본값으로 저장된다")
    void saveBuilderCreatedBoardPersistsInitialState() {
        Member member = Member.builder()
                .providerType(LoginProviderType.KAKAO)
                .providerId("provider-id")
                .role(MemberRole.USER)
                .build();
        entityManager.persist(member);

        RoomType roomType = BeanUtils.instantiateClass(RoomType.class);
        ReflectionTestUtils.setField(roomType, "name", "One-room");
        ReflectionTestUtils.setField(roomType, "isDeleted", false);
        entityManager.persist(roomType);

        Region region = BeanUtils.instantiateClass(Region.class);
        ReflectionTestUtils.setField(region, "name", "Seoul");
        ReflectionTestUtils.setField(region, "scope", 1);
        entityManager.persist(region);

        RoommateBoard board = RoommateBoard.builder()
                .member(member)
                .title("Looking for a roommate")
                .contents("Quiet home near the station")
                .deposit(10_000)
                .monthlyRent(500)
                .managementCost(50)
                .roomType(roomType)
                .region(region)
                .comeableDate(LocalDateTime.of(2026, 7, 1, 9, 0))
                .build();

        RoommateBoard savedBoard = roommateBoardRepository.saveAndFlush(board);
        entityManager.clear();

        RoommateBoard foundBoard = roommateBoardRepository.findById(savedBoard.getId()).orElseThrow();
        assertThat(foundBoard.getIsDeleted()).isFalse();
        assertThat(foundBoard.getHits()).isZero();
    }
}
