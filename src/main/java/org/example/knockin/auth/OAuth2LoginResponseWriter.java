package org.example.knockin.auth;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.example.knockin.controller.auth.LoginResponse;
import org.example.knockin.global.api.CommonResponse;
import org.example.knockin.global.exception.ErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
public class OAuth2LoginResponseWriter {

    private final ObjectMapper objectMapper;

    public OAuth2LoginResponseWriter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void writeSuccess(HttpServletResponse response, LoginResponse loginResponse) throws IOException {
        response.setStatus(HttpStatus.OK.value());
        write(response, CommonResponse.status(HttpStatus.OK).body(loginResponse));
    }

    public void writeFailure(HttpServletResponse response, ErrorCode errorCode) throws IOException {
        response.setStatus(errorCode.getHttpStatus().value());
        write(response, CommonResponse.status(errorCode.getHttpStatus()).body(errorCode));
    }

    private void write(HttpServletResponse response, CommonResponse<?> body) throws IOException {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        objectMapper.writeValue(response.getWriter(), body);
    }
}
