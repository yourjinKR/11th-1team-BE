package org.example.knockin.auth;

import org.example.knockin.entity.member.MemberRole;
import org.example.knockin.entity.member.MemberStatus;

public record AuthMember(
        Long memberId,
        MemberStatus status,
        MemberRole role
) {
}
