package org.example.knockin.global.auth.service;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.knockin.verification.entity.LoginProviderType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientResponseException;

@Service
@Slf4j
@RequiredArgsConstructor
public class KakaoOauth2DeleteService extends AbstractOauth2DeleteService{
    @Value("${kakao.admin.key}")
    private String kakaoAdminKey;
    private final KakaoUnlinkClient kakaoUnlinkClient;

    @Override
    @CircuitBreaker(name = "kakoUnlink", fallbackMethod = "unlinkFallback")
    public boolean requestUnlink(String providerId) {
        String response = kakaoUnlinkClient.unlink(
                "KakaoAK " + kakaoAdminKey,
                "user_id",
                providerId
        );
        return StringUtils.hasText(response);
    }

    public boolean unlinkFallback(String providerId, Exception e) {
        if (e instanceof CallNotPermittedException) {
            log.warn("카카오 연결 끊기(Unlink) 생략. 서킷이 열려 있습니다.");
        } else if (e instanceof RestClientResponseException responseException) {
            log.warn("카카오 연결 끊기(Unlink) 실패. HTTP status: {}", responseException.getStatusCode());
        } else {
            log.warn("카카오 연결 끊기(Unlink) 실패. 오류 유형: {}", e.getClass().getSimpleName());
        }
        return false;
    }

    @Override
    public LoginProviderType getProviderType() {
        return LoginProviderType.KAKAO;
    }
}
