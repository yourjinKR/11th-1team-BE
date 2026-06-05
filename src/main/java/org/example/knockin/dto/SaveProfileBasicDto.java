package org.example.knockin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.knockin.entity.member.Gender;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class SaveProfileBasicDto {
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Request {
        private String name;
        private LocalDate birth;
        private Gender gender;
        private String email;
        private List<Long> terms;
    }

    @Data
    @Builder
    public static class Response {
        private LocalDateTime updatedAt;
    }
}
