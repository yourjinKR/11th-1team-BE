package org.example.knockin.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class BoVerificationCompanyListDto {
    @Data
    public static class Request {
    }

    @Data
    public static class Response {
        @Schema(description = "employee auth")
        private List<EmployeeAuthItem> employeeAuth;

        @Data
        public static class EmployeeAuthItem {
            @Schema(description = "수락 여부")
            private Boolean isAccepted;
            @Schema(description = "이메일")
            private String email;
            @Schema(description = "생성 일시")
            private LocalDateTime createAt;
        }
    }
}