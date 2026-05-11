package org.example.knockin.auth.provider;

import org.example.knockin.entity.member.LoginProvider;

public record SocialUserInfo(
        LoginProvider provider,
        String providerId
) {
}
