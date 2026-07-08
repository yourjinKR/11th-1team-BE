package org.example.knockin.auth.service;

import lombok.RequiredArgsConstructor;
import org.example.knockin.entity.member.Member;
import org.example.knockin.auth.util.OAuth2UserInfoProvider;
import org.example.knockin.dto.OAuth2UserInfo;
import org.example.knockin.dto.PrincipalDetails;
import org.example.knockin.service.impl.MemberServiceImpl;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    private final MemberServiceImpl memberService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        Map<String, Object> oAuth2UserAttributes = super.loadUser(userRequest).getAttributes();
        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        String userNameAttributeName = userRequest.getClientRegistration().getProviderDetails()
                .getUserInfoEndpoint().getUserNameAttributeName();

        Class<? extends OAuth2UserInfo> infoClass = OAuth2UserInfoProvider
                .findByRegistrationId(registrationId)
                .getInfoClass();

        OAuth2UserInfo oAuth2UserInfo = objectMapper.convertValue(oAuth2UserAttributes, infoClass);
        Member member = memberService.getOrSave(oAuth2UserInfo);

        return new PrincipalDetails(member, oAuth2UserAttributes, userNameAttributeName);
    }
}
