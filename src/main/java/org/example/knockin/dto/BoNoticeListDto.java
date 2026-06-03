package org.example.knockin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class BoNoticeListDto {
    @Data
    @Schema(name = "BoNoticeListRequest")
    public static class Request {
    }

    @Data
    @Schema(name = "BoNoticeListResponse")
    public static class Response {
        @Schema(description = "공지사항 목록")
        private List<NoticeItem> notices;

        @Data
        @Schema(name = "BoNoticeListNoticeItem")
        public static class NoticeItem {
            @Schema(description = "고유 식별 ID")
            private Long id;
            @Schema(description = "제목")
            private String title;
            @Schema(description = "작성자")
            private String writer;
            @Schema(description = "날짜 및 시간")
            private LocalDateTime createAt;
            @Schema(description = "타입/유형")
            private String type;
        }
    }
}
