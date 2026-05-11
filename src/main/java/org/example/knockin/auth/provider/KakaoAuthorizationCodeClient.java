package org.example.knockin.auth.provider;

import java.util.Map;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.extern.slf4j.Slf4j;
import org.example.knockin.auth.AuthErrorCode;
import org.example.knockin.entity.member.LoginProvider;
import org.example.knockin.global.exception.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

@Component
@Slf4j
public class KakaoAuthorizationCodeClient implements SocialOAuthClient {

    @Value("${auth.kakao.token-uri}")
    private String tokenUri;

    @Value("${auth.kakao.user-info-uri}")
    private String userInfoUri;

    @Value("${auth.kakao.client-id}")
    private String clientId;

    @Value("${auth.kakao.client-secret}")
    private String clientSecret;

    @Value("${auth.kakao.redirect-uri}")
    private String redirectUri;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public boolean supports(LoginProvider provider, AuthPlatform platform, SocialCredentialType credentialType) {
        return provider == LoginProvider.KAKAO
                && platform == AuthPlatform.WEB
                && credentialType == SocialCredentialType.AUTHORIZATION_CODE;
    }

    @Override
    public SocialUserInfo getUserInfo(SocialLoginCommand command) {
        if (!StringUtils.hasText(command.credential())) {
            throw new BusinessException(AuthErrorCode.INVALID_PROVIDER_TOKEN);
        }

        try {
            String accessToken = requestAccessToken(command.credential());
            return requestUserInfo(accessToken);
        } catch (Exception e) {
            log.warn("[카카오 OAuth] 인가 코드 로그인 흐름에 실패했습니다. 사유={}", e.getMessage());
            throw new BusinessException(AuthErrorCode.INVALID_PROVIDER_TOKEN);
        }
    }

    private String requestAccessToken(String authorizationCode) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("client_id", clientId);
        body.add("redirect_uri", redirectUri);
        body.add("code", authorizationCode);
        if (StringUtils.hasText(clientSecret)) {
            body.add("client_secret", clientSecret);
        }

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);
        ResponseEntity<Map> responseEntity = restTemplate.exchange(
                tokenUri,
                HttpMethod.POST,
                request,
                Map.class
        );

        Map<?, ?> response = responseEntity.getBody();
        Object accessToken = response == null ? null : response.get("access_token");
        if (accessToken == null || !StringUtils.hasText(String.valueOf(accessToken))) {
            throw new BusinessException(AuthErrorCode.INVALID_PROVIDER_TOKEN);
        }
        return String.valueOf(accessToken);
    }

    private SocialUserInfo requestUserInfo(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<KakaoUserInfoResponse> responseEntity = restTemplate.exchange(
                userInfoUri,
                HttpMethod.GET,
                request,
                KakaoUserInfoResponse.class
        );

        KakaoUserInfoResponse response = responseEntity.getBody();
        if (response == null || response.id() == null) {
            throw new BusinessException(AuthErrorCode.INVALID_PROVIDER_TOKEN);
        }

        log.info("[카카오 OAuth] 카카오 사용자 정보를 조회했습니다. kakaoId={}, profileImageUrl={} name={}",
                response.id(),
                response.profileImageUrl(),
                response.profileName()
        );
        return new SocialUserInfo(LoginProvider.KAKAO, String.valueOf(response.id()));
    }

    private record KakaoUserInfoResponse(
            Long id,
            @JsonProperty("kakao_account")
            KakaoAccount kakaoAccount
    ) {
        String profileImageUrl() {
            if (kakaoAccount == null || kakaoAccount.profile() == null) { return null;}
            return kakaoAccount.profile().profileImageUrl();
        }

        String profileName() {
            if (kakaoAccount == null || kakaoAccount.profile() == null) { return null;}
            return kakaoAccount.profile().nickname();
        }
    }

    private record KakaoAccount(
            KakaoProfile profile
    ) {
    }

    private record KakaoProfile(
            @JsonProperty("profile_image_url")
            String profileImageUrl,
            String nickname
    ) {
    }
}
