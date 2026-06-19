package org.example.knockin.config;

import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import org.example.knockin.global.api.CommonResponse;
import org.example.knockin.global.api.ErrorResponse;
import org.example.knockin.global.auth.exception.AuthErrorCode;
import org.example.knockin.global.auth.exception.AuthException;
import org.example.knockin.global.exception.ErrorCode;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.socket.messaging.StompSubProtocolErrorHandler;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
public class StompErrorHandler extends StompSubProtocolErrorHandler {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Message<byte[]> handleClientMessageProcessingError(Message<byte[]> clientMessage, Throwable ex) {
        ErrorCode errorCode = resolveErrorCode(ex);
        byte[] payload = writeErrorPayload(errorCode);

        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.ERROR);
        accessor.setMessage(errorCode.getMessage());
        accessor.setContentType(MimeTypeUtils.APPLICATION_JSON);
        accessor.setLeaveMutable(true);

        return MessageBuilder.createMessage(payload, accessor.getMessageHeaders());
    }

    private ErrorCode resolveErrorCode(Throwable ex) {
        Throwable current = ex;
        while (current != null) {
            if (current instanceof AuthException authException) {
                return authException.getErrorCode();
            }
            current = current.getCause();
        }
        return AuthErrorCode.AUTHENTICATION_FAILED;
    }

    private byte[] writeErrorPayload(ErrorCode errorCode) {
        try {
            CommonResponse<?> payload = CommonResponse.status(errorCode.getHttpStatus()).error(ErrorResponse.of(errorCode));
            return objectMapper.writeValueAsString(payload).getBytes(StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to serialize STOMP error payload.", e);
        }
    }
}
