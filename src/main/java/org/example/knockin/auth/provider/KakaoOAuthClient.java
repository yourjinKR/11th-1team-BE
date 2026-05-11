package org.example.knockin.auth.provider;

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
public class KakaoOAuthClient implements SocialOAuthClient {

    @Value("${auth.kakao.user-info-uri}")
    private String userInfoUri;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public LoginProvider supports() {
        return LoginProvider.KAKAO;
    }

    @Override
    public SocialUserInfo getUserInfo(String providerAccessToken) {
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
            throw new BusinessException(AuthErrorCode.INVALID_PROVIDER_TOKEN);
        }
    }

    private record KakaoUserInfoResponse(
            Long id
    ) {
    }
}
