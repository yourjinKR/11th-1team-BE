package org.example.knockin.auth.util;

import java.security.Principal;
import org.example.knockin.dto.PrincipalDetails;
import org.example.knockin.exception.AuthErrorCode;
import org.example.knockin.exception.AuthException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class PrincipalMemberResolver {

    public Long resolveMemberId(Principal principal) {
        return resolvePrincipalDetails(principal).getMember().getId();
    }

    public PrincipalDetails resolvePrincipalDetails(Principal principal) {
        if (!(principal instanceof Authentication authentication)
                || !(authentication.getPrincipal() instanceof PrincipalDetails details)) {
            throw new AuthException(AuthErrorCode.AUTHENTICATION_FAILED);
        }

        return details;
    }
}
