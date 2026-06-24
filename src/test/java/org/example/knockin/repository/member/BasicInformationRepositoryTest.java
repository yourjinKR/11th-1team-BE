package org.example.knockin.repository.member;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.EntityManager;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.time.LocalDate;
import java.util.List;
import org.example.knockin.config.QueryDslConfig;
import org.example.knockin.entity.auth.LoginProviderType;
import org.example.knockin.entity.file.BasicInformationFile;
import org.example.knockin.entity.file.File;
import org.example.knockin.entity.file.FileType;
import org.example.knockin.entity.member.BasicInformation;
import org.example.knockin.entity.member.Gender;
import org.example.knockin.entity.member.Member;
import org.example.knockin.entity.member.MemberRole;
import org.example.knockin.repository.member.row.ChattingRoomBasicInfoRow;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

@DataJpaTest
@ActiveProfiles("test")
@Import(QueryDslConfig.class)
@DisplayName("기본 정보 리포지토리")
class BasicInformationRepositoryTest {

    @Autowired
    private BasicInformationRepository basicInformationRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("채팅 요청 상세 기본 정보 목록은 회원별 최신 기본 정보와 최신 프로필 이미지를 반환한다")
    void findChattingRoomBasicInfoRowsReturnsLatestBasicInformationAndProfileByMember() {
        // Given
        Member requester = persistMember("requester");
        Member requestee = persistMember("requestee");
        Member unrelated = persistMember("unrelated");

        BasicInformation oldRequesterInfo = persistBasicInformation(requester, "이전요청자", LocalDate.of(1999, 1, 1), Gender.MALE);
        persistBasicInformationFile(oldRequesterInfo, "old-requester.jpg");
        BasicInformation latestRequesterInfo = persistBasicInformation(requester, "최신요청자", LocalDate.of(2000, 2, 2), Gender.FEMALE);
        persistBasicInformationFile(latestRequesterInfo, "requester-profile-1.jpg");
        persistBasicInformationFile(latestRequesterInfo, "requester-profile-2.jpg");

        BasicInformation requesteeInfo = persistBasicInformation(requestee, "피요청자", LocalDate.of(2001, 3, 3), Gender.MALE);
        persistBasicInformationFile(requesteeInfo, "requestee-profile.jpg");
        persistBasicInformation(unrelated, "무관회원", LocalDate.of(2002, 4, 4), Gender.FEMALE);

        entityManager.flush();
        entityManager.clear();

        // When
        List<ChattingRoomBasicInfoRow> rows = basicInformationRepository.findChattingRoomBasicInfoRows(
                List.of(requester.getId(), requestee.getId())
        );

        // Then
        assertThat(rows).hasSize(2);
        assertThat(rows).extracting(ChattingRoomBasicInfoRow::memberId)
                .containsExactlyInAnyOrder(requester.getId(), requestee.getId());

        ChattingRoomBasicInfoRow requesterRow = findByMemberId(rows, requester.getId());
        assertThat(requesterRow.name()).isEqualTo("최신요청자");
        assertThat(requesterRow.birth()).isEqualTo(LocalDate.of(2000, 2, 2));
        assertThat(requesterRow.gender()).isEqualTo(Gender.FEMALE);
        assertThat(requesterRow.profileImageUrl()).isEqualTo("requester-profile-2.jpg");

        ChattingRoomBasicInfoRow requesteeRow = findByMemberId(rows, requestee.getId());
        assertThat(requesteeRow.name()).isEqualTo("피요청자");
        assertThat(requesteeRow.birth()).isEqualTo(LocalDate.of(2001, 3, 3));
        assertThat(requesteeRow.gender()).isEqualTo(Gender.MALE);
        assertThat(requesteeRow.profileImageUrl()).isEqualTo("requestee-profile.jpg");
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

    private BasicInformation persistBasicInformation(Member member, String name, LocalDate birth, Gender gender) {
        BasicInformation basicInformation = BasicInformation.builder()
                .member(member)
                .name(name)
                .birth(birth)
                .gender(gender)
                .email(name + "@example.com")
                .build();
        entityManager.persist(basicInformation);
        return basicInformation;
    }

    private void persistBasicInformationFile(BasicInformation basicInformation, String savedFileName) {
        File file = File.builder()
                .type(FileType.USER_PROFILE_IMAGE)
                .originalFileName(savedFileName)
                .savedFileName(savedFileName)
                .fileExt("jpg")
                .isDeleted(false)
                .build();
        entityManager.persist(file);

        BasicInformationFile basicInformationFile = newBasicInformationFile(basicInformation, file);
        entityManager.persist(basicInformationFile);
    }

    private BasicInformationFile newBasicInformationFile(BasicInformation basicInformation, File file) {
        try {
            Constructor<BasicInformationFile> constructor = BasicInformationFile.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            BasicInformationFile basicInformationFile = constructor.newInstance();
            ReflectionTestUtils.setField(basicInformationFile, "basicInformation", basicInformation);
            ReflectionTestUtils.setField(basicInformationFile, "file", file);
            return basicInformationFile;
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new IllegalStateException("기본 정보 파일 테스트 엔티티 생성에 실패했습니다.", e);
        }
    }

    private ChattingRoomBasicInfoRow findByMemberId(List<ChattingRoomBasicInfoRow> rows, Long memberId) {
        return rows.stream()
                .filter(row -> row.memberId().equals(memberId))
                .findFirst()
                .orElseThrow();
    }
}
