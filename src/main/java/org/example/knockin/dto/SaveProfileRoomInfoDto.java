package org.example.knockin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.example.knockin.entity.room.RoomProfileType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class SaveProfileRoomInfoDto {
    @Data
    @Schema(name = "SaveProfileRoomInfoRequest")
    public static class Request {
        private RoomProfileType type;
        private Integer minDeposit;
        private Integer maxDeposit;
        private Integer minMounthRent;
        private Integer maxMounthRent;
        private LocalDateTime comeEnableAt;
        private List<Long> region;
        private List<Long> roomProfile;
        private Integer deposit;
        private Integer mounthRent;
    }

    @Data
    @Schema(name = "SaveProfileRoomInfoResponse")
    public static class Response {
        private LocalDateTime updatedAt;
    }
}
