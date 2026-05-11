package org.example.knockin.controller.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.example.knockin.auth.provider.AuthPlatform;
import org.example.knockin.auth.provider.SocialCredentialType;
import org.example.knockin.auth.provider.SocialLoginCommand;
import org.example.knockin.entity.member.LoginProvider;

public record SocialLoginRequest(
        @NotNull(message = "provider는 필수입니다.")
        LoginProvider provider,

        @NotNull(message = "platform은 필수입니다.")
        AuthPlatform platform,

        @NotNull(message = "credentialType은 필수입니다.")
        SocialCredentialType credentialType,

        @NotBlank(message = "credential은 필수입니다.")
        String credential
) {
    public SocialLoginCommand toCommand() {
        return new SocialLoginCommand(provider, platform, credentialType, credential);
    }
}
