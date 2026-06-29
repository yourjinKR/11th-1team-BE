package org.example.knockin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Compatibility {
    @Schema(description = "점수")
    private Integer score;

    @Schema(description = "라이프스타일 정보 목록")
    private List<LifeStyleInfo> lifeStyleInfo;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LifeStyleInfo {
        @Schema(description = "제목")
        private String title;

        @Schema(description = "백분율")
        private Integer percent;
    }
}
