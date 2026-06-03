package org.example.knockin.global.auth.service;

import org.example.knockin.entity.auth.LoginProviderType;
import org.springframework.web.client.RestTemplate;

public class AbstractOauth2DeleteService implements Oauth2DeleteService{
    protected final RestTemplate restTemplate;

    public AbstractOauth2DeleteService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public boolean requestUnlink(String providerId) {
        return false;
    }

    public LoginProviderType getProviderType() {
        return null;
    }
}
