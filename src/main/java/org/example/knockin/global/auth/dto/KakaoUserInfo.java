package org.example.knockin.global.auth.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.knockin.entity.auth.LoginProviderType;

public class KakaoUserInfo implements OAuth2UserInfo {
    private Long id;

    @JsonProperty("properties")
    private Properties properties;

    @Getter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Properties {
        private String nickname;
        @JsonProperty("profile_image")
        private String profileImage;
        @JsonProperty("thumbnail_image")
        private String thumbnailImage;
    }

    @JsonProperty("kakao_account")
    private KakaoAccount kakaoAccount;

    @Getter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class KakaoAccount {
        private String email;
        @JsonProperty("profile")
        private Profile profile;

        @Getter
        @NoArgsConstructor
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Profile {
            private String nickname;
            @JsonProperty("profile_image_url")
            private String profileImageUrl;
            @JsonProperty("thumbnail_image_url")
            private String thumbnailImageUrl;
        }
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public LoginProviderType getProviderType() {
        return LoginProviderType.KAKAO;
    }
}