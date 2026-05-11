package org.example.knockin.auth;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationFacade {

    public AuthMember getCurrentMember() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null
                || !(authentication.getPrincipal() instanceof AuthMember authenticatedMember)) {
            throw new IllegalArgumentException("임시 예외");
        }

        return authenticatedMember;
    }
}