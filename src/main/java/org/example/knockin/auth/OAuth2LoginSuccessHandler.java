package org.example.knockin.auth;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.knockin.auth.provider.SocialUserInfo;
import org.example.knockin.controller.auth.LoginResponse;
import org.example.knockin.service.impl.AuthServiceImpl;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final OAuth2UserInfoExtractorResolver userInfoExtractorResolver;
    private final AuthServiceImpl authServiceImpl;
    private final OAuth2LoginResponseWriter responseWriter;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {
        try {
            OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
            OAuth2UserInfoExtractor extractor = userInfoExtractorResolver.resolve(oauthToken);
            SocialUserInfo userInfo = extractor.extract(oauthToken);
            LoginResponse loginResponse = authServiceImpl.loginWithVerifiedSocialUser(userInfo);

            clearOAuthSession(request);
            responseWriter.writeSuccess(response, loginResponse);
        } catch (Exception e) {
            log.error("[OAuth2 Login] 로그인 후처리에 실패했습니다. uri={}, exception={}, message={}",
                    request.getRequestURI(),
                    e.getClass().getSimpleName(),
                    e.getMessage(),
                    e
            );

            clearOAuthSession(request);
            if (!response.isCommitted()) {
                responseWriter.writeFailure(response, AuthErrorCode.OAUTH2_LOGIN_FAILED);
            }
        }
    }

    private void clearOAuthSession(HttpServletRequest request) {
        SecurityContextHolder.clearContext();
        if (request.getSession(false) != null) {
            request.getSession(false).invalidate();
        }
    }

}
