package org.example.knockin.controller.auth;

import jakarta.validation.constraints.NotBlank;

public record KakaoLoginRequest(
        @NotBlank(message = "providerAccessToken은 필수입니다.")
        String providerAccessToken
) {
}
