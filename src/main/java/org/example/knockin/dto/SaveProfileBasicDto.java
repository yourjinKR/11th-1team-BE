package org.example.knockin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.example.knockin.entity.member.Gender;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class SaveProfileBasicDto {
    @Data
    @Schema(name = "SaveProfileBasicRequest")
    public static class Request {
        private String name;
        private LocalDate birth;
        private Gender gender;
        private String email;
        private List<Long> terms;
    }

    @Data
    @Schema(name = "SaveProfileBasicResponse")
    public static class Response {
        private LocalDateTime updatedAt;
    }
}
