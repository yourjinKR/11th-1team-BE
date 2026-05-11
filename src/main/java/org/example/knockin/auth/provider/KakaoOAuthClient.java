package org.example.knockin.auth.provider;

import lombok.extern.slf4j.Slf4j;
import org.example.knockin.auth.AuthErrorCode;
import org.example.knockin.entity.member.LoginProvider;
import org.example.knockin.global.exception.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

@Component
@Slf4j
public class KakaoOAuthClient implements SocialOAuthClient {

    @Value("${auth.kakao.user-info-uri}")
    private String userInfoUri;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public boolean supports(LoginProvider provider, AuthPlatform platform, SocialCredentialType credentialType) {
        return provider == LoginProvider.KAKAO
                && credentialType == SocialCredentialType.ACCESS_TOKEN
                && (platform == AuthPlatform.APP || platform == AuthPlatform.WEB);
    }

    @Override
    public SocialUserInfo getUserInfo(SocialLoginCommand command) {
        String providerAccessToken = command.credential();
        if (!StringUtils.hasText(providerAccessToken)) {
            throw new BusinessException(AuthErrorCode.INVALID_PROVIDER_TOKEN);
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(providerAccessToken);
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

            return new SocialUserInfo(LoginProvider.KAKAO, String.valueOf(response.id()));
        } catch (Exception e) {
            log.warn("[카카오 OAuth] access token 로그인 흐름에 실패했습니다. platform={}, 사유={}",
                    command.platform(),
                    e.getMessage()
            );
            throw new BusinessException(AuthErrorCode.INVALID_PROVIDER_TOKEN);
        }
    }

    private record KakaoUserInfoResponse(
            Long id
    ) {
    }
}
