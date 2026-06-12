package org.example.knockin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.knockin.entity.member.MemberPrivacyType;

import java.time.LocalDateTime;

@Data
public class ProfileVisibilityDto {
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Request {
        private MemberPrivacyType status;
    }

    @Data
    @Builder
    public static class Response {
        private LocalDateTime updatedAt;
    }
}
