package org.example.knockin.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.knockin.auth.provider.JwtTokenProvider;
import org.example.knockin.auth.provider.JwtTokenProvider.IssuedAccessToken;
import org.example.knockin.auth.provider.SocialOAuthClient;
import org.example.knockin.auth.provider.SocialOAuthClientResolver;
import org.example.knockin.auth.provider.AuthPlatform;
import org.example.knockin.auth.provider.SocialCredentialType;
import org.example.knockin.auth.provider.SocialLoginCommand;
import org.example.knockin.auth.provider.SocialUserInfo;
import org.example.knockin.controller.auth.LoginResponse;
import org.example.knockin.controller.auth.OnBoardingNextStep;
import org.example.knockin.entity.MemberEntity;
import org.example.knockin.entity.member.LoginProvider;
import org.example.knockin.repository.MemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl {
    private final MemberRepository memberRepository;
    private final SocialOAuthClientResolver socialOAuthClientResolver;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public LoginResponse loginWithKakao(String providerAccessToken) {
        return login(new SocialLoginCommand(
                LoginProvider.KAKAO,
                AuthPlatform.APP,
                SocialCredentialType.ACCESS_TOKEN,
                providerAccessToken
        ));
    }

    @Transactional
    public LoginResponse login(SocialLoginCommand command) {
        SocialOAuthClient socialOAuthClient = socialOAuthClientResolver.resolve(command);
        SocialUserInfo userInfo = socialOAuthClient.getUserInfo(command);
        return loginWithVerifiedSocialUser(userInfo);
    }

    @Transactional
    public LoginResponse loginWithVerifiedSocialUser(SocialUserInfo userInfo) {
        MemberEntity member = memberRepository.findByProviderAndProviderId(userInfo.provider(), userInfo.providerId())
                .orElseGet(() -> {
                    MemberEntity newMember = MemberEntity.pendingMember(
                            userInfo.provider(),
                            userInfo.providerId()
                    );
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
