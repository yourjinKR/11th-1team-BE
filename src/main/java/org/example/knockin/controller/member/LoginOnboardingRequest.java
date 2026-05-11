package org.example.knockin.controller.member;

import java.util.List;

public record LoginOnboardingRequest(
        String name,
        List<Agreement> agreements
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
