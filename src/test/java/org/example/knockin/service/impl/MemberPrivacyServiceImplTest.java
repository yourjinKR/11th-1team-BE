package org.example.knockin.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.example.knockin.entity.member.MemberPrivacy;
import org.example.knockin.entity.member.MemberPrivacyType;
import org.example.knockin.repository.member.MemberPrivacyRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("회원 공개범위 서비스")
class MemberPrivacyServiceImplTest {

    @Mock
    private MemberPrivacyRepository memberPrivacyRepository;

    @InjectMocks
    private MemberPrivacyServiceImpl memberPrivacyService;

    @Test
    @DisplayName("회원 ID로 공개범위 목록을 조회하고 결과를 그대로 반환한다")
    void findByMemberIdReturnsMemberPrivacyList() {
        // Given
        Long memberId = 1L;
        List<MemberPrivacy> memberPrivacies = List.of(MemberPrivacy.builder()
                .type(MemberPrivacyType.PUBLIC)
                .build());
        when(memberPrivacyRepository.findByMemberId(memberId)).thenReturn(memberPrivacies);

        // When
        List<MemberPrivacy> result = memberPrivacyService.findByMemberId(memberId);

        // Then
        assertThat(result).isSameAs(memberPrivacies);
        verify(memberPrivacyRepository).findByMemberId(memberId);
    }
}
