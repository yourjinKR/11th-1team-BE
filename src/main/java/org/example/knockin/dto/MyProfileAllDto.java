package org.example.knockin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import org.example.knockin.entity.life.LifePatternType;
import org.example.knockin.entity.room.RoomProfileType;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class MyProfileAllDto {
    @Data
    public static class Request {
    }

    @Data
    @Builder
    public static class Response {
        @Schema(description = "라이프스타일 목록")
        private List<Lifestyle> lifestyles;
        @Schema(description = "타입/유형")
        private RoomProfileType type;
        @Schema(description = "최소 보증금")
        private Integer minDeposit;
        @Schema(description = "최대 보증금")
        private Integer maxDeposit;
        @Schema(description = "최소 월세")
        private Integer minMounthRent;
        @Schema(description = "최대 월세")
        private Integer maxMounthRent;
        @Schema(description = "입주 가능일")
        private LocalDateTime comeEnableAt;
        @Schema(description = "지역 목록")
        private List<Region> region;
        @Schema(description = "방 프로필 목록")
        private List<RoomProfile> roomProfile;
        @Schema(description = "보증금")
        private Integer deposit;
        @Schema(description = "월세")
        private Integer mounthRent;

        @Data
        public static class Lifestyle {
            @Schema(description = "고유 식별 ID")
            private Long lifestyleId;
            @Schema(description = "이름")
            private String name;
            @Schema(description = "값")
            private String value;
            @Schema(description = "설명")
            private String description;
            @Schema(description = "타입/유형")
            private LifePatternType type;
        }

        @Data
        public static class Region {
            @Schema(description = "고유 식별 ID")
            private Long regionId;
            @Schema(description = "지역명")
            private String region;
        }

        @Data
        public static class RoomProfile {
            @Schema(description = "고유 식별 ID")
            private Long roomProfileId;
            @Schema(description = "방 프로필 이름")
            private String roomProfileName;
        }
    }
}
