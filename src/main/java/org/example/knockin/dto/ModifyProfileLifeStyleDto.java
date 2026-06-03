package org.example.knockin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ModifyProfileLifeStyleDto {
    @Data
    @Schema(name = "ModifyProfileLifeStyleRequest")
    public static class Request {
        private List<Long> lifestyles;
    }

    @Data
    @Schema(name = "ModifyProfileLifeStyleResponse")
    public static class Response {
        private LocalDateTime updatedAt;
    }
}
