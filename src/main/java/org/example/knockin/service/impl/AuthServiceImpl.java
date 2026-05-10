package org.example.knockin.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.knockin.auth.provider.JwtTokenProvider;
import org.example.knockin.auth.provider.JwtTokenProvider.IssuedAccessToken;
import org.example.knockin.auth.provider.KakaoOAuthClient;
import org.example.knockin.auth.provider.SocialUserInfo;
import org.example.knockin.controller.LoginResponse;
import org.example.knockin.controller.OnBoardingNextStep;
import org.example.knockin.entity.MemberEntity;
import org.example.knockin.entity.member.LoginProvider;
import org.example.knockin.repository.MemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl {
    private final MemberRepository memberRepository;
    private final KakaoOAuthClient kakaoOAuthClient;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public LoginResponse loginWithKakao(String providerAccessToken) {
        SocialUserInfo userInfo = kakaoOAuthClient.getUserInfo(providerAccessToken);
        return login(LoginProvider.KAKAO, userInfo.providerId());
    }

    @Transactional
    public LoginResponse login(LoginProvider provider, String providerId) {
        MemberEntity member = memberRepository.findByProviderAndProviderId(provider, providerId)
                .orElseGet(() -> {
                    MemberEntity newMember = MemberEntity.pendingMember(provider, providerId);
                    return memberRepository.save(newMember);
                });

        OnBoardingNextStep nextStep = OnBoardingNextStep.from(member.getStatus());
        IssuedAccessToken issuedAccessToken = jwtTokenProvider.createToken(member);

        return LoginResponse.of(
                issuedAccessToken.raw(),
                "Bearer",
                issuedAccessToken.expiresIn(),
                member,
                nextStep
        );
    }
}
