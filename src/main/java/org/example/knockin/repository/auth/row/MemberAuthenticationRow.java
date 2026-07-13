package org.example.knockin.repository.auth.row;

import org.example.knockin.entity.auth.AuthenticationType;

public record MemberAuthenticationRow(
        Long memberId,
        AuthenticationType type
) {
}
