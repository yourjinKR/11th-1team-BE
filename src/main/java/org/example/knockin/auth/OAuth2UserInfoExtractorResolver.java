package org.example.knockin.auth;

import java.util.List;
import org.example.knockin.global.exception.BusinessException;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Component;

@Component
public class OAuth2UserInfoExtractorResolver {

    private final List<OAuth2UserInfoExtractor> extractors;

    public OAuth2UserInfoExtractorResolver(List<OAuth2UserInfoExtractor> extractors) {
        this.extractors = extractors;
    }

    public OAuth2UserInfoExtractor resolve(OAuth2AuthenticationToken authentication) {
        String registrationId = authentication.getAuthorizedClientRegistrationId();
        return extractors.stream()
                .filter(extractor -> extractor.supports(registrationId))
                .findFirst()
                .orElseThrow(() -> new BusinessException(AuthErrorCode.UNSUPPORTED_PROVIDER));
    }
}
