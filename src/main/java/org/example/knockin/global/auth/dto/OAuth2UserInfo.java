package org.example.knockin.global.auth.dto;

import org.example.knockin.entity.auth.LoginProviderType;

public interface OAuth2UserInfo {
    Long getId();
    LoginProviderType getProviderType();
}
