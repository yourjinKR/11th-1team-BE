package org.example.knockin.repository.auth;

import java.util.List;
import org.example.knockin.entity.auth.AuthenticationType;

public interface AuthenticationRepositoryCustom {
    List<AuthenticationType> getAcceptedAuthenticationTypeByMemberId(Long memberId);
}
