package org.example.knockin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.knockin.entity.member.Gender;
import org.example.knockin.entity.room.RoomProfileType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ModifyProfileAllDto {
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Request {
        private String name;
        private LocalDate birth;
        private Gender gender;
        private String email;
        private List<Long> terms;
        private List<LifeStyleInfo> lifestyles;
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
        private boolean isComeableAtNegotiable;

        @Data
        @AllArgsConstructor
        @NoArgsConstructor
        public static class LifeStyleInfo {
            private Long id;
            private Long lifestyleId;
        }
    }

    @Data
    @Builder
    public static class Response {
        private LocalDateTime updatedAt;
    }
}
