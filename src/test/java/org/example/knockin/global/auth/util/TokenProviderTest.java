package org.example.knockin.global.auth.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Optional;
import org.example.knockin.entity.auth.LoginProviderType;
import org.example.knockin.entity.member.Member;
import org.example.knockin.entity.member.MemberRole;
import org.example.knockin.global.auth.dto.PrincipalDetails;
import org.example.knockin.repository.member.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class TokenProviderTest {
    private static final String JWT_KEY =
            "test-jwt-key-test-jwt-key-test-jwt-key-test-jwt-key-test-jwt-key-test-jwt-key";

    @Mock
    private MemberRepository memberRepository;

    private TokenProvider tokenProvider;

    @BeforeEach
    void setUp() {
        tokenProvider = new TokenProvider(memberRepository);
        ReflectionTestUtils.setField(tokenProvider, "key", JWT_KEY);
        ReflectionTestUtils.invokeMethod(tokenProvider, "setSecretKey");
    }

    @Test
    void restoresPrincipalDetailsFromAccessToken() {
        Member member = Member.builder()
                .id(1L)
                .providerType(LoginProviderType.KAKAO)
                .providerId("provider-id")
                .role(MemberRole.USER)
                .build();
        PrincipalDetails principal = new PrincipalDetails(member, Map.of("id", "provider-id"), "id");
        Authentication loginAuthentication = new UsernamePasswordAuthenticationToken(
                principal,
                null,
                principal.getAuthorities()
        );
        when(memberRepository.findById(member.getId())).thenReturn(Optional.of(member));

        String accessToken = tokenProvider.generateAccessToken(loginAuthentication);
        Authentication restoredAuthentication = tokenProvider.getAuthentication(accessToken);

        assertThat(restoredAuthentication.getPrincipal()).isInstanceOf(PrincipalDetails.class);
        PrincipalDetails restoredPrincipal = (PrincipalDetails) restoredAuthentication.getPrincipal();
        assertThat(restoredPrincipal.getMember()).isEqualTo(member);
        assertThat(restoredAuthentication.getName()).isEqualTo(member.getProviderId());
    }
}
