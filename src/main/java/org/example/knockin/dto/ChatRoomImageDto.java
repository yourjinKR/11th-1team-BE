package org.example.knockin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
public class ChatRoomImageDto {

    @Data
    public static class Request {

    }

    @Data
    @Builder
    @AllArgsConstructor
    public static class Response {
        @Schema(description = "이미지 URL 목록")
        private List<String> imageUrls;
    }
}
