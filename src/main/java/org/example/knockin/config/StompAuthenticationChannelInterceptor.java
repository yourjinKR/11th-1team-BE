package org.example.knockin.config;

import java.security.Principal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.example.knockin.global.auth.exception.AuthErrorCode;
import org.example.knockin.global.auth.exception.AuthException;
import org.example.knockin.global.auth.util.TokenConstants;
import org.example.knockin.global.auth.util.TokenProvider;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
public class StompAuthenticationChannelInterceptor implements ChannelInterceptor {
    public static final String TOKEN_EXPIRES_AT_SESSION_KEY = "TOKEN_EXPIRES_AT";

    private final TokenProvider tokenProvider;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null || accessor.getCommand() == null) {
            return message;
        }

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String accessToken = resolveAccessToken(accessor);
            Authentication authentication = authenticate(accessToken);
            Date expiresAt = tokenProvider.getExpiration(accessToken);

            accessor.setUser(authentication);
            getSessionAttributes(accessor).put(TOKEN_EXPIRES_AT_SESSION_KEY, expiresAt);
        } else if (StompCommand.SEND.equals(accessor.getCommand()) || StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            authenticateSession(accessor);
        }

        return message;
    }

    private Authentication authenticate(String accessToken) {
        if (!tokenProvider.validateToken(accessToken)) {
            throw new AuthException(AuthErrorCode.TOKEN_INVALID);
        }

        return tokenProvider.getAuthentication(accessToken);
    }

    private void authenticateSession(StompHeaderAccessor accessor) {
        Principal principal = accessor.getUser();
        if (!(principal instanceof Authentication authentication) || !authentication.isAuthenticated()) {
            throw new AuthException(AuthErrorCode.AUTHENTICATION_FAILED);
        }

        Object expiresAt = getSessionAttributes(accessor).get(TOKEN_EXPIRES_AT_SESSION_KEY);
        if (!(expiresAt instanceof Date tokenExpiresAt)) {
            throw new AuthException(AuthErrorCode.AUTHENTICATION_FAILED);
        }

        if (!tokenExpiresAt.after(new Date())) {
            throw new AuthException(AuthErrorCode.TOKEN_EXPIRED);
        }
    }

    private String resolveAccessToken(StompHeaderAccessor accessor) {
        String authorization = accessor.getFirstNativeHeader(TokenConstants.AUTHORIZATION_HEADER);
        if (!StringUtils.hasText(authorization) || !authorization.startsWith(TokenConstants.BEARER_PREFIX)) {
            throw new AuthException(AuthErrorCode.TOKEN_INVALID);
        }

        String accessToken = authorization.substring(TokenConstants.BEARER_PREFIX.length());
        if (!StringUtils.hasText(accessToken)) {
            throw new AuthException(AuthErrorCode.TOKEN_INVALID);
        }

        return accessToken;
    }

    private Map<String, Object> getSessionAttributes(StompHeaderAccessor accessor) {
        Map<String, Object> sessionAttributes = accessor.getSessionAttributes();
        if (sessionAttributes == null) {
            sessionAttributes = new HashMap<>();
            accessor.setSessionAttributes(sessionAttributes);
        }
        return sessionAttributes;
    }
}
