package org.example.knockin.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class MyVerificationListDto {
    @Data
    public static class Request {
    }

    @Data
    public static class Response {
        private AuthInfo studentAuth;
        private AuthInfo employeeAuth;

        @Data
        public static class AuthInfo {
            private Boolean isAccepted;
            private String email;
            private LocalDateTime createAt;
        }
    }
}
