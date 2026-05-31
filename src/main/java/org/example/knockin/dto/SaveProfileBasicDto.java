package org.example.knockin.dto;

import lombok.Data;
import org.example.knockin.entity.member.Gender;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class SaveProfileBasicDto {
    @Data
    public static class Request {
        private String name;
        private LocalDate birth;
        private Gender gender;
        private String email;
        private List<Long> terms;
    }

    @Data
    public static class Response {
        private LocalDateTime updatedAt;
    }
}
