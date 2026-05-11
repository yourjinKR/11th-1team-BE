package org.example.knockin.auth.provider;

import org.example.knockin.entity.member.LoginProvider;

public interface SocialOAuthClient {
    boolean supports(LoginProvider provider, AuthPlatform platform, SocialCredentialType credentialType);

    SocialUserInfo getUserInfo(SocialLoginCommand command);
}
