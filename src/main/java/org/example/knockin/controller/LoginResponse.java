package org.example.knockin.controller;

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

    public static LoginResponse of(String accessToken, String tokenType, long expiresIn, MemberEntity member, OnBoardingNextStep nextStep) {
        return new LoginResponse(
                accessToken,
                tokenType,
                expiresIn,
                member.getMemberId(),
                member.getStatus(),
                member.getRole(),
                nextStep
        );
    }
}
