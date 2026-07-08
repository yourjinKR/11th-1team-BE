package org.example.knockin.auth.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.example.knockin.exception.AuthErrorCode;
import org.example.knockin.exception.AuthException;
import org.example.knockin.auth.handler.SecurityErrorResponseWriter;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
public class TokenExceptionFilter extends OncePerRequestFilter {
    private final SecurityErrorResponseWriter securityErrorResponseWriter;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            filterChain.doFilter(request, response);
        } catch (AuthException e) {
            securityErrorResponseWriter.write(response, e.getErrorCode());
        } catch (io.jsonwebtoken.JwtException e) {
            securityErrorResponseWriter.write(response, AuthErrorCode.TOKEN_INVALID);
        }
    }
}
