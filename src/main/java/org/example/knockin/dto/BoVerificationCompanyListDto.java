package org.example.knockin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class BoVerificationCompanyListDto {
    @Data
    @Schema(name = "BoVerificationCompanyListRequest")
    public static class Request {
    }

    @Data
    @Schema(name = "BoVerificationCompanyListResponse")
    public static class Response {
        private List<EmployeeAuthItem> employeeAuth;

        @Data
        @Schema(name = "BoVerificationCompanyListEmployeeAuthItem")
        public static class EmployeeAuthItem {
            private Boolean isAccepted;
            private String email;
            private LocalDateTime createAt;
        }
    }
}
