package org.example.knockin.auth;

import org.example.knockin.auth.provider.SocialUserInfo;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;

public interface OAuth2UserInfoExtractor {

    boolean supports(String registrationId);

    SocialUserInfo extract(OAuth2AuthenticationToken authentication);
}
