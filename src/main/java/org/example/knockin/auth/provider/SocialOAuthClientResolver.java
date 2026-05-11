package org.example.knockin.auth.provider;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.example.knockin.auth.AuthErrorCode;
import org.example.knockin.entity.member.LoginProvider;
import org.example.knockin.global.exception.BusinessException;
import org.springframework.stereotype.Component;

@Component
public class SocialOAuthClientResolver {
    private final Map<LoginProvider, SocialOAuthClient> clients;

    public SocialOAuthClientResolver(List<SocialOAuthClient> clients) {
        this.clients = new EnumMap<>(LoginProvider.class);
        for (SocialOAuthClient client : clients) {
            this.clients.put(client.supports(), client);
        }
    }

    public SocialOAuthClient resolve(LoginProvider provider) {
        SocialOAuthClient client = clients.get(provider);
        if (client == null) {
            throw new BusinessException(AuthErrorCode.UNSUPPORTED_PROVIDER);
        }
        return client;
    }
}
