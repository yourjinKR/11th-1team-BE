package org.example.knockin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class MyVerificationListDto {
    @Data
    @Schema(name = "MyVerificationListRequest")
    public static class Request {
    }

    @Data
    @Schema(name = "MyVerificationListResponse")
    public static class Response {
        private AuthInfo studentAuth;
        private AuthInfo employeeAuth;

        @Data
        @Schema(name = "MyVerificationListAuthInfo")
        public static class AuthInfo {
            private Boolean isAccepted;
            private String email;
            private LocalDateTime createAt;
        }
    }
}
