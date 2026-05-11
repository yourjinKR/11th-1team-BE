package org.example.knockin.controller.auth;

import org.example.knockin.entity.member.MemberStatus;

// TODO: UX를 고려하여 url 경로를 지정하는 형식 고려
public enum OnBoardingNextStep {
    ONBOARDING,
    HOME;

    public static OnBoardingNextStep from(MemberStatus memberStatus) {
        return memberStatus == MemberStatus.ACTIVE ? HOME : ONBOARDING;
    }
}
