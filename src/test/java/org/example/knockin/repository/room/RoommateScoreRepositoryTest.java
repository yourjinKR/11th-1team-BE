package org.example.knockin.repository.room;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.EntityManager;
import java.util.List;
import org.example.knockin.config.QueryDslConfig;
import org.example.knockin.dto.Compatibility;
import org.example.knockin.entity.auth.LoginProviderType;
import org.example.knockin.entity.chat.ChattingRequired;
import org.example.knockin.entity.chat.ChattingRequiredStatus;
import org.example.knockin.entity.chat.ChattingRoom;
import org.example.knockin.entity.life.LifePattern;
import org.example.knockin.entity.life.LifePatternInformation;
import org.example.knockin.entity.life.LifePatternType;
import org.example.knockin.entity.life.MemberLifePatternLog;
import org.example.knockin.entity.life.PreferenceConditionWeightLog;
import org.example.knockin.entity.member.Member;
import org.example.knockin.entity.member.MemberRole;
import org.example.knockin.entity.room.MyRoommate;
import org.example.knockin.entity.room.RoommateMatchingRequired;
import org.example.knockin.entity.room.RoommateRequiredStatus;
import org.example.knockin.entity.room.RoommateScore;
import org.example.knockin.service.impl.JavaRoommateScoreService;
import org.example.knockin.service.impl.RoommateScorePolicy;
import org.hibernate.Hibernate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
@Import(QueryDslConfig.class)
@DisplayName("룸메이트 점수 리포지토리")
class RoommateScoreRepositoryTest {

    @Autowired
    private RoommateScoreRepository roommateScoreRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("내 룸메이트 점수 상세 조회는 궁합 계산에 필요한 연관 정보를 함께 조회한다")
    void findWithScoreDetailsByMyRoommateIdFetchesAssociationsForCompatibilityCalculation() {
        // Given
        Member evaluator = persistMember("score-evaluator");
        Member target = persistMember("score-target");
        MyRoommate myRoommate = persistMyRoommate(evaluator, target);
        LifePattern lifePattern = persistLifePattern("청결 민감도", 1);
        LifePatternInformation information = persistLifePatternInformation(lifePattern, "3");
        MemberLifePatternLog lifePatternLog = persistMemberLifePatternLog(evaluator, information);
        PreferenceConditionWeightLog weightLog = persistPreferenceConditionWeightLog(evaluator, lifePattern);
        persistRoommateScore(myRoommate, lifePatternLog, weightLog, 80);
        entityManager.flush();
        entityManager.clear();

        // When
        List<RoommateScore> scores = roommateScoreRepository.findWithScoreDetailsByMyRoommateId(myRoommate.getId());
        RoommateScore score = scores.getFirst();

        // Then
        assertThat(scores).hasSize(1);
        assertThat(Hibernate.isInitialized(score.getLifePatternInformationLog())).isTrue();
        assertThat(Hibernate.isInitialized(score.getLifePatternInformationLog().getMember())).isTrue();
        assertThat(Hibernate.isInitialized(score.getLifePatternInformationLog().getLifePatternInformation())).isTrue();
        assertThat(Hibernate.isInitialized(score.getLifePatternInformationLog().getLifePatternInformation().getLifePattern())).isTrue();
        assertThat(Hibernate.isInitialized(score.getPreferenceConditionWeightLog())).isTrue();

        entityManager.clear();
        Compatibility compatibility = roommateScoreService().calculateRoommateCompatibility(evaluator.getId(), scores);
        assertThat(compatibility.getTotalScore()).isEqualTo(80);
        assertThat(compatibility.getLifeStyleInfo())
                .extracting(Compatibility.LifeStyleInfo::getName, Compatibility.LifeStyleInfo::getPercent)
                .containsExactly(org.assertj.core.groups.Tuple.tuple("청결 민감도", 80));
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

    private MyRoommate persistMyRoommate(Member requester, Member requestee) {
        ChattingRequired chattingRequired = ChattingRequired.builder()
                .requester(requester)
                .requestee(requestee)
                .status(ChattingRequiredStatus.ACCEPTED)
                .build();
        entityManager.persist(chattingRequired);

        ChattingRoom chattingRoom = ChattingRoom.builder()
                .chattingRequired(chattingRequired)
                .build();
        entityManager.persist(chattingRoom);

        RoommateMatchingRequired matchingRequired = RoommateMatchingRequired.builder()
                .requester(requester)
                .requestee(requestee)
                .chattingRoom(chattingRoom)
                .status(RoommateRequiredStatus.ACCEPTED)
                .build();
        entityManager.persist(matchingRequired);

        MyRoommate myRoommate = MyRoommate.builder()
                .roommateMatchingRequired(matchingRequired)
                .isDeleted(false)
                .build();
        entityManager.persist(myRoommate);
        return myRoommate;
    }

    private LifePattern persistLifePattern(String name, Integer sort) {
        LifePattern lifePattern = LifePattern.builder()
                .name(name)
                .dtype(LifePatternType.SCALE)
                .isDeleted(false)
                .sort(sort)
                .build();
        entityManager.persist(lifePattern);
        return lifePattern;
    }

    private LifePatternInformation persistLifePatternInformation(LifePattern lifePattern, String value) {
        LifePatternInformation information = LifePatternInformation.builder()
                .lifePattern(lifePattern)
                .dvalue(value)
                .description(value)
                .build();
        entityManager.persist(information);
        return information;
    }

    private MemberLifePatternLog persistMemberLifePatternLog(Member member, LifePatternInformation information) {
        MemberLifePatternLog log = MemberLifePatternLog.builder()
                .member(member)
                .lifePatternInformation(information)
                .build();
        entityManager.persist(log);
        return log;
    }

    private PreferenceConditionWeightLog persistPreferenceConditionWeightLog(Member member, LifePattern lifePattern) {
        PreferenceConditionWeightLog log = PreferenceConditionWeightLog.builder()
                .member(member)
                .lifePattern(lifePattern)
                .build();
        entityManager.persist(log);
        return log;
    }

    private void persistRoommateScore(
            MyRoommate myRoommate,
            MemberLifePatternLog lifePatternLog,
            PreferenceConditionWeightLog weightLog,
            Integer score
    ) {
        RoommateScore roommateScore = RoommateScore.builder()
                .myRoommate(myRoommate)
                .lifePatternInformationLog(lifePatternLog)
                .preferenceConditionWeightLog(weightLog)
                .score(score)
                .build();
        entityManager.persist(roommateScore);
    }

    private JavaRoommateScoreService roommateScoreService() {
        RoommateScorePolicy roommateScorePolicy = new RoommateScorePolicy();
        roommateScorePolicy.setPerfectScore(100);
        roommateScorePolicy.setImportantPatternMultiplier(2);

        return new JavaRoommateScoreService(
                null,
                null,
                null,
                null,
                roommateScorePolicy,
                null,
                null,
                null
        );
    }
}
