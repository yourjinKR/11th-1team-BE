package org.example.knockin.dto;

import lombok.Data;
import org.example.knockin.entity.life.LifePatternType;
import java.util.List;

@Data
public class BoLifeStylePatternDetailDto {
    @Data
    public static class Request {
    }

    @Data
    public static class Response {
        private Long id;
        private String name;
        private LifePatternType type;
        private List<DetailItem> details;

        @Data
        public static class DetailItem {
            private String values;
            private String description;
        }
    }
}
