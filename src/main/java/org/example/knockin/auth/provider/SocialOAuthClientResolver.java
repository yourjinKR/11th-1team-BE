package org.example.knockin.auth.provider;

import java.util.List;
import org.example.knockin.auth.AuthErrorCode;
import org.example.knockin.global.exception.BusinessException;
import org.springframework.stereotype.Component;

@Component
public class SocialOAuthClientResolver {
    private final List<SocialOAuthClient> clients;

    public SocialOAuthClientResolver(List<SocialOAuthClient> clients) {
        this.clients = clients;
    }

    public SocialOAuthClient resolve(SocialLoginCommand command) {
        return clients.stream()
                .filter(client -> client.supports(
                        command.provider(),
                        command.platform(),
                        command.credentialType()
                ))
                .findFirst()
                .orElseThrow(() -> new BusinessException(AuthErrorCode.UNSUPPORTED_PROVIDER));
    }
}
