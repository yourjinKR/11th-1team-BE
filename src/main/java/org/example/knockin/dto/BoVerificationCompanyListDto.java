package org.example.knockin.dto;

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
        private List<EmployeeAuthItem> employeeAuth;

        @Data
        public static class EmployeeAuthItem {
            private Boolean isAccepted;
            private String email;
            private LocalDateTime createAt;
        }
    }
}
