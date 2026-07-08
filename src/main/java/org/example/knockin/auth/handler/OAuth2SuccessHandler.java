package org.example.knockin.auth.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

import lombok.RequiredArgsConstructor;
import org.example.knockin.entity.member.Member;
import org.example.knockin.global.api.CommonResponse;
import org.example.knockin.dto.AuthResponse;
import org.example.knockin.dto.PrincipalDetails;
import org.example.knockin.exception.AuthErrorCode;
import org.example.knockin.auth.util.TokenConstants;
import org.example.knockin.auth.util.TokenProvider;
import org.example.knockin.global.KnockInProps;
import org.example.knockin.exception.BusinessException;
import org.example.knockin.service.impl.MemberServiceImpl;
import org.jspecify.annotations.NullMarked;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import tools.jackson.databind.ObjectMapper;


@NullMarked
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {
    private final TokenProvider tokenProvider;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final KnockInProps knockInProps;
    private final MemberServiceImpl memberService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        String accessToken = tokenProvider.generateAccessToken(authentication);
        PrincipalDetails principalDetails = (PrincipalDetails) authentication.getPrincipal();

        if(ObjectUtils.isEmpty(principalDetails)) throw new BusinessException(AuthErrorCode.ILLEGAL_LOGIN_ACCESS);
        Member member = principalDetails.getMember();
        AuthResponse authResponse = memberService.findMemberForLogin(member, accessToken);
        CommonResponse<AuthResponse> commonResponse = CommonResponse.status(HttpStatus.OK).body(authResponse);

        if (request.getAttribute("isSdkLogin") != null) {
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(objectMapper.writeValueAsString(commonResponse));
        } else {
            boolean secureCookie = knockInProps.getClientSuccessUrl().startsWith("https://");

            ResponseCookie accessTokenCookie = ResponseCookie.from(TokenConstants.ACCESS_TOKEN_COOKIE_NAME, accessToken)
                    .httpOnly(true)
                    .secure(secureCookie)
                    .sameSite(secureCookie ? "None" : "Lax")
                    .path("/")
                    .maxAge(TokenProvider.ACCESS_TOKEN_EXPIRE_DURATION)
                    .build();

            response.addHeader(HttpHeaders.SET_COOKIE, accessTokenCookie.toString());
            response.sendRedirect(knockInProps.getClientSuccessUrl());
        }
    }
}
