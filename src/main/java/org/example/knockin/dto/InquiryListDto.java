package org.example.knockin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class InquiryListDto {
    @Data
    @Schema(name = "InquiryListRequest")
    public static class Request {
    }

    @Data
    @Schema(name = "InquiryListResponse")
    public static class Response {
        @Schema(description = "문의 목록")
        private List<InquiryItem> inquiries;

        @Data
        @Schema(name = "InquiryListInquiryItem")
        public static class InquiryItem {
            @Schema(description = "고유 식별 ID")
            private Long id;
            @Schema(description = "제목")
            private String title;
            @Schema(description = "작성자")
            private String writer;
            @Schema(description = "상태")
            private String status;
            @Schema(description = "날짜 및 시간")
            private LocalDateTime createAt;
            @Schema(description = "타입/유형")
            private String type;
        }
    }
}
