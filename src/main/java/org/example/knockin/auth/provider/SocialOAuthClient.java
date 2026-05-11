package org.example.knockin.auth.provider;

import org.example.knockin.entity.member.LoginProvider;

public interface SocialOAuthClient {
    LoginProvider supports();

    SocialUserInfo getUserInfo(String providerToken);
}
