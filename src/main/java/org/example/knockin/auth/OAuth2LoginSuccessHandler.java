package org.example.knockin.auth;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import org.example.knockin.auth.provider.SocialUserInfo;
import org.example.knockin.controller.auth.LoginResponse;
import org.example.knockin.global.api.CommonResponse;
import org.example.knockin.service.impl.AuthServiceImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final OAuth2UserInfoExtractorResolver userInfoExtractorResolver;
    private final AuthServiceImpl authServiceImpl;
    private final ObjectMapper objectMapper;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {
        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
        OAuth2UserInfoExtractor extractor = userInfoExtractorResolver.resolve(oauthToken);
        SocialUserInfo userInfo = extractor.extract(oauthToken);
        LoginResponse loginResponse = authServiceImpl.loginWithVerifiedSocialUser(userInfo);

        clearOAuthSession(request);
        writeLoginResponse(response, loginResponse);
    }

    private void clearOAuthSession(HttpServletRequest request) {
        SecurityContextHolder.clearContext();
        if (request.getSession(false) != null) {
            request.getSession(false).invalidate();
        }
    }

    private void writeLoginResponse(HttpServletResponse response, LoginResponse loginResponse) throws IOException {
        response.setStatus(HttpStatus.OK.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        CommonResponse<LoginResponse> body = CommonResponse.status(HttpStatus.OK).body(loginResponse);
        objectMapper.writeValue(response.getWriter(), body);
    }
}
