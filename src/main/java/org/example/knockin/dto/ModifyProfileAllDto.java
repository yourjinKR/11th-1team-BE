package org.example.knockin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.example.knockin.entity.member.Gender;
import org.example.knockin.entity.room.RoomProfileType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ModifyProfileAllDto {
    @Data
    @Schema(name = "ModifyProfileAllRequest")
    public static class Request {
        private String name;
        private LocalDate birth;
        private Gender gender;
        private String email;
        private List<Long> terms;
        private List<Long> lifestyles;
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
    @Schema(name = "ModifyProfileAllResponse")
    public static class Response {
        private LocalDateTime updatedAt;
    }
}
