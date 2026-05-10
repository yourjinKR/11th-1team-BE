package org.example.knockin.auth.provider;

import org.springframework.stereotype.Component;

@Component
public class KakaoOAuthClient {
    public SocialUserInfo getUserInfo(String providerAccessToken) {
        return new SocialUserInfo(providerAccessToken);
    }
}
