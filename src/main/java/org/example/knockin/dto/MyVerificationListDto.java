package org.example.knockin.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class MyVerificationListDto {
    @Data
    public static class Request {
    }

    @Data
    public static class Response {
        @Schema(description = "학생 auth")
        private AuthInfo studentAuth;
        @Schema(description = "employee auth")
        private AuthInfo employeeAuth;

        @Data
        public static class AuthInfo {
            @Schema(description = "수락 여부")
            private Boolean isAccepted;
            @Schema(description = "이메일")
            private String email;
            @Schema(description = "생성 일시")
            private LocalDateTime createAt;
        }
    }
}