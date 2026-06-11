package org.example.knockin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
public class BoardDto {
    @Data
    @Schema(name = "BoardSaveRequest")
    public static class Request {
        @NotNull
        @Schema(description = "제목")
        private String title;

        @NotNull
        @Schema(description = "내용")
        private String contents;

        @NotNull
        @Schema(description = "보증금")
        private int deposit;

        @NotNull
        @Schema(description = "월세")
        private int mountlyRent;

        @NotNull
        @Schema(description = "관리비")
        private int managementCost;

        @NotNull
        @Schema(description = "방 타입 ID")
        private long roomTypeId;

        @NotNull
        @Schema(description = "지역 ID")
        private long regionId;

        @Schema(description = "입주 가능일")
        private LocalDateTime comeableAt;

        @Valid
        @Size(max = 10)
        @Schema(description = "이미지 목록")
        private List<FileDto> images;

        @AssertTrue(message = "썸네일 이미지는 최소 1개 이상 포함되어야 합니다.")
        @Schema(hidden = true)
        public boolean isThumbnailIncluded() {
            return images != null &&
                    images.stream().anyMatch(FileDto::isThumbnail);
        }

        @Data
        @Schema(name = "BoardFileRequest")
        public static class FileDto {
            @NotNull
            @Schema(description = "이미지 URL")
            private MultipartFile file;

            @NotNull
            @Schema(description = "썸네일 여부")
            private boolean thumbnail;
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(name = "BoardSaveResponse")
    public static class Response {
        @Schema(description = "날짜 및 시간")
        private LocalDateTime updatedAt;
    }
}
