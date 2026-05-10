package org.example.knockin.controller;

import org.example.knockin.entity.member.MemberStatus;

public enum OnBoardingNextStep {
    ONBOARDING,
    HOME;

    public static OnBoardingNextStep from(MemberStatus memberStatus) {
        return memberStatus == MemberStatus.ACTIVE ? HOME : ONBOARDING;
    }
}
