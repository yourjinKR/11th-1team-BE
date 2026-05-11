package org.example.knockin.auth;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2LoginFailureHandler implements AuthenticationFailureHandler {

    private final OAuth2LoginResponseWriter responseWriter;

    @Override
    public void onAuthenticationFailure(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception
    ) throws IOException, ServletException {
        log.warn("[OAuth2 Login] 로그인에 실패했습니다. uri={}, providerError={}, exception={}, message={}",
                request.getRequestURI(),
                request.getParameter("error"),
                exception.getClass().getSimpleName(),
                exception.getMessage()
        );

        clearOAuthSession(request);
        responseWriter.writeFailure(response, AuthErrorCode.OAUTH2_LOGIN_FAILED);
    }

    private void clearOAuthSession(HttpServletRequest request) {
        SecurityContextHolder.clearContext();
        if (request.getSession(false) != null) {
            request.getSession(false).invalidate();
        }
    }
}
