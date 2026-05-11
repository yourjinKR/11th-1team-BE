package org.example.knockin.auth;

import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.example.knockin.auth.provider.SocialUserInfo;
import org.example.knockin.entity.member.LoginProvider;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class KakaoOAuth2UserInfoExtractor implements OAuth2UserInfoExtractor {
    private static final String REGISTRATION_ID = "kakao";

    @Override
    public boolean supports(String registrationId) {
        return REGISTRATION_ID.equals(registrationId);
    }

    @Override
    public SocialUserInfo extract(OAuth2AuthenticationToken authentication) {
        Map<String, Object> attributes = authentication.getPrincipal().getAttributes();
        Object kakaoId = attributes.get("id");
        if (kakaoId == null) {
            throw new IllegalArgumentException("Kakao OAuth2 user id is missing.");
        }

        logProfile(attributes, kakaoId);
        return new SocialUserInfo(LoginProvider.KAKAO, String.valueOf(kakaoId));
    }

    @SuppressWarnings("unchecked")
    private void logProfile(Map<String, Object> attributes, Object kakaoId) {
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        Map<String, Object> profile = kakaoAccount == null ? null : (Map<String, Object>) kakaoAccount.get("profile");

        Object nickname = profile == null ? null : profile.get("nickname");
        Object profileImageUrl = profile == null ? null : profile.get("profile_image_url");

        log.info("[카카오 OAuth2 Login] 사용자 정보를 조회했습니다. kakaoId={}, nickname={}, profileImageUrl={}",
                kakaoId,
                nickname,
                profileImageUrl
        );
    }
}
