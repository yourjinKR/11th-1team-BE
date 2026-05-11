package org.example.knockin.controller.auth;

import org.example.knockin.auth.provider.JwtTokenProvider.IssuedAccessToken;
import org.example.knockin.entity.MemberEntity;
import org.example.knockin.entity.member.MemberRole;
import org.example.knockin.entity.member.MemberStatus;

public record LoginResponse(
        String accessToken,
        String tokenType,
        long expiresIn,
        long memberId,
        MemberStatus memberStatus,
        MemberRole role,
        OnBoardingNextStep nextStep
) {

    public static LoginResponse of(IssuedAccessToken issuedAccessToken, String tokenType, MemberEntity member) {
        return new LoginResponse(
                issuedAccessToken.raw(),
                tokenType,
                issuedAccessToken.expiresIn(),
                member.getMemberId(),
                member.getStatus(),
                member.getRole(),
                OnBoardingNextStep.from(member.getStatus())
        );
    }
}
