package org.example.knockin.controller;

import lombok.RequiredArgsConstructor;
import org.example.knockin.entity.member.LoginProvider;
import org.example.knockin.global.api.CommonResponse;
import org.example.knockin.service.impl.AuthServiceImpl;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final AuthServiceImpl authServiceImpl;

    @PostMapping("/login/kakao")
    public CommonResponse<LoginResponse> kakaoLogin(@RequestBody KakaoLoginRequest request) {
        LoginResponse response = authServiceImpl.loginWithKakao(request.providerAccessToken());
        return CommonResponse.status(HttpStatus.OK).body(response);
    }
}
