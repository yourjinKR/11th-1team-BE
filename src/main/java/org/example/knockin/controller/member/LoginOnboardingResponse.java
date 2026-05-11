package org.example.knockin.controller.member;

import org.example.knockin.controller.auth.OnBoardingNextStep;
import org.example.knockin.entity.MemberEntity;
import org.example.knockin.entity.member.MemberStatus;

public record LoginOnboardingResponse(
        long memberId,
        MemberStatus memberStatus,
        OnBoardingNextStep nextStep
) {
    public static LoginOnboardingResponse completedOf(MemberEntity member) {
        return new LoginOnboardingResponse(
                member.getMemberId(),
                member.getStatus(),
                OnBoardingNextStep.HOME
        );
    }
}
