package org.example.knockin.controller.member;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.List;

public record LoginOnboardingRequest(
        @NotBlank(message = "name은 필수입니다.")
        String name,
        List<@Valid Agreement> agreements
) {

    public record Agreement(
            AgreementType type,
            boolean status,
            String version
    ) {
    }

    public enum AgreementType {
        TERMS_OF_SERVICE,
        PRIVACY_POLICY
    }
}
