package org.example.knockin.controller.member;

import jakarta.validation.Valid;
import org.example.knockin.global.api.CommonResponse;
import org.example.knockin.service.impl.MemberServiceImpl;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/members")
public class MemberController {

    private final MemberServiceImpl memberServiceImpl;

    public MemberController(MemberServiceImpl memberServiceImpl) {
        this.memberServiceImpl = memberServiceImpl;
    }

    @PatchMapping("/me/onboarding")
    public CommonResponse<LoginOnboardingResponse> completeOnboarding(
            @Valid @RequestBody LoginOnboardingRequest request
    ) {
        LoginOnboardingResponse response = memberServiceImpl.completeOnboarding(request);
        return CommonResponse.status(HttpStatus.OK).body(response);
    }
}
