package org.example.knockin.repository.chat;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.EntityManager;
import org.example.knockin.config.QueryDslConfig;
import org.example.knockin.entity.auth.LoginProviderType;
import org.example.knockin.entity.chat.ChattingRequired;
import org.example.knockin.entity.chat.ChattingRequiredStatus;
import org.example.knockin.entity.member.Member;
import org.example.knockin.entity.member.MemberRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
@Import(QueryDslConfig.class)
@DisplayName("채팅 요청 Repository")
class ChattingRequiredRepositoryTest {

    @Autowired
    private ChattingRequiredRepository chattingRequiredRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("역방향 채팅 요청이 이미 있으면 두 회원 사이에 요청이 있다고 조회한다")
    void existsBetweenMembersReturnsTrueWhenReverseRequestExists() {
        // Given
        Member memberA = persistMember("provider-a");
        Member memberB = persistMember("provider-b");
        persistChattingRequired(memberB, memberA);
        entityManager.flush();
        entityManager.clear();

        // When
        boolean exists = chattingRequiredRepository.existsBetweenMembers(memberA, memberB);

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("두 회원 사이의 채팅 요청이 없으면 없다고 조회한다")
    void existsBetweenMembersReturnsFalseWhenRequestDoesNotExist() {
        // Given
        Member memberA = persistMember("provider-a");
        Member memberB = persistMember("provider-b");
        Member otherMember = persistMember("provider-c");
        persistChattingRequired(memberA, otherMember);
        entityManager.flush();
        entityManager.clear();

        // When
        boolean exists = chattingRequiredRepository.existsBetweenMembers(memberA, memberB);

        // Then
        assertThat(exists).isFalse();
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

    private ChattingRequired persistChattingRequired(Member requester, Member requestee) {
        ChattingRequired chattingRequired = ChattingRequired.builder()
                .requester(requester)
                .requestee(requestee)
                .status(ChattingRequiredStatus.ACCEPTED)
                .build();
        entityManager.persist(chattingRequired);
        return chattingRequired;
    }
}
