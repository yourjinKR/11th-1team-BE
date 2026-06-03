package org.example.knockin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class BoVerificationCompanyDto {
    @Data
    @Schema(name = "BoVerificationCompanyRequest")
    public static class Request {
    }

    @Data
    @Schema(name = "BoVerificationCompanyResponse")
    public static class Response {
        private LocalDateTime updatedAt;
    }
}
