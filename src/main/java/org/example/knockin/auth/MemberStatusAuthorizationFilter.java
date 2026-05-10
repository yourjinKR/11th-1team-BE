package org.example.knockin.auth;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.example.knockin.entity.member.MemberStatus;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class MemberStatusAuthorizationFilter extends OncePerRequestFilter {
    private static final String ONBOARDING_PATH = "/members/me/onboarding";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !(authentication.getPrincipal() instanceof AuthMember member)) {
            filterChain.doFilter(request, response);
            return;
        }

        if (member.status() == MemberStatus.DELETED) {
            reject(response);
            return;
        }

        if (isOnboardingRequest(request)) {
            if (member.status() != MemberStatus.PENDING) {
                reject(response);
                return;
            }

            filterChain.doFilter(request, response);
            return;
        }

        if (member.status() != MemberStatus.ACTIVE) {
            reject(response);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean isOnboardingRequest(HttpServletRequest request) {
        return HttpMethod.PATCH.matches(request.getMethod())
                && ONBOARDING_PATH.equals(request.getRequestURI());
    }

    private void reject(HttpServletResponse response) throws IOException {
        response.sendError(HttpStatus.FORBIDDEN.value(), "Forbidden member status.");
    }
}
