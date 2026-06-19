package org.example.knockin.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.knockin.entity.room.RoomProfileType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ModifyProfileRoomInfoDto {
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Request {
        @Schema(description = "유형")
        private RoomProfileType type;
        @Schema(description = "min deposit")
        private Integer minDeposit;
        @Schema(description = "max deposit")
        private Integer maxDeposit;
        @Schema(description = "min mounth rent")
        private Integer minMounthRent;
        @Schema(description = "max mounth rent")
        private Integer maxMounthRent;
        @Schema(description = "come enable at")
        private LocalDateTime comeEnableAt;
        @Schema(description = "region")
        private List<Long> region;
        @Schema(description = "방 프로필")
        private List<Long> roomProfile;
        @Schema(description = "deposit")
        private Integer deposit;
        @Schema(description = "mounth rent")
        private Integer mounthRent;
        @Schema(description = "is comeable at negotiable")
        private boolean isComeableAtNegotiable;
    }

    @Data
    @Builder
    public static class Response {
        @Schema(description = "수정 일시")
        private LocalDateTime updatedAt;
    }
}