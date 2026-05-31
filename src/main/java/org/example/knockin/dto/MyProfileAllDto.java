package org.example.knockin.dto;

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
    public static class Response {
        private List<Lifestyle> lifestyles;
        private RoomProfileType type;
        private Integer minDeposit;
        private Integer maxDeposit;
        private Integer minMounthRent;
        private Integer maxMounthRent;
        private LocalDateTime comeEnableAt;
        private List<Region> region;
        private List<RoomProfile> roomProfile;
        private Integer deposit;
        private Integer mounthRent;

        @Data
        public static class Lifestyle {
            private Long lifestyleId;
            private String name;
            private String value;
            private String description;
            private LifePatternType type;
        }

        @Data
        public static class Region {
            private Long regionId;
            private String region;
        }

        @Data
        public static class RoomProfile {
            private Long roomProfileId;
            private String roomProfileName;
        }
    }
}
