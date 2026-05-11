package org.example.knockin.auth.provider;

import org.example.knockin.entity.member.LoginProvider;

public record SocialLoginCommand(
        LoginProvider provider,
        AuthPlatform platform,
        SocialCredentialType credentialType,
        String credential
) {
}
