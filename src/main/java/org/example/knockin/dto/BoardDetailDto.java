package org.example.knockin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.knockin.entity.auth.AuthenticationType;
import org.example.knockin.entity.life.LifePatternType;
import java.time.LocalDateTime;
import java.util.List;
import org.example.knockin.entity.member.Gender;
import org.example.knockin.global.util.DateUtils;
import org.example.knockin.global.util.StringUtils;

@Data
@NoArgsConstructor
public class BoardDetailDto {

    @Data
    public static class Request {
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        @Schema(description = "기본 정보")
        private BasicInfo basicInfo;

        @Schema(description = "이미지 정보 목록")
        private List<FileDetailDto> images;

        @Schema(description = "방 추가 옵션 목록")
        private List<String> roomExtraOptionNames;

        @Schema(description = "고정 상단 생활패턴")
        private List<Lifestyle> primaryLifeStyles;

        @Schema(description = "더보기 생활패턴")
        private List<Lifestyle> additionalLifeStyles;

        @Schema(description = "선호 룸메이트 조건 목록")
        private List<Condition> conditions;

        @Schema(description = "승인된 신원 인증")
        private List<AuthenticationType> authentications;

        @Schema(description = "적합도")
        private Compatibility compatibility;

        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class BasicInfo {
            @Schema(description = "고유 식별 ID")
            private Long boardId;

            @Schema(description = "제목")
            private String title;

            @Schema(description = "보증금")
            private Integer deposit;

            @Schema(description = "관리비")
            private Integer managementCost;

            @Schema(description = "월세")
            private Integer monthlyRent;

            @Schema(description = "방 타입명")
            private String roomTypeName;

            @Schema(description = "지역명 풀네임")
            private String regionFullName;

            @Schema(description = "날짜 및 시간")
            private LocalDateTime createdAt;

            @Schema(description = "조회수")
            private Long hits;

            @Schema(description = "내용")
            private String contents;

            @Schema(description = "사용자 식별 값")
            private Long memberId;

            @Schema(description = "등록자 이름")
            private String memberName;

            @Schema(description = "등록자 프로필 사진 URL")
            private String memberProfileImageUrl;

            @Schema(description = "등록자 나이")
            private Integer memberAge;

            @Schema(description = "등록자 성별")
            private Gender gender;

            public BasicInfo(Long boardId, String title, Integer deposit, Integer managementCost, Integer monthlyRent,
                             String roomTypeName, String boardRegion, String parentRegion, String grandParentRegion, LocalDateTime createdAt, Long hits,
                             String contents, Long memberId, String memberName, String memberProfileImageUrl, LocalDate birth, Gender gender) {

                String regionFullName = StringUtils.parseToRegionFullName(grandParentRegion, parentRegion, boardRegion);
                int age = DateUtils.calculateAge(birth);

                this.boardId = boardId;
                this.title = title;
                this.deposit = deposit;
                this.managementCost = managementCost;
                this.monthlyRent = monthlyRent;
                this.roomTypeName = roomTypeName;
                this.regionFullName = regionFullName;
                this.createdAt = createdAt;
                this.hits = hits;
                this.contents = contents;
                this.memberId = memberId;
                this.memberName = memberName;
                this.memberProfileImageUrl = memberProfileImageUrl;
                this.memberAge = age;
                this.gender = gender;
            }
        }

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class FileDetailDto {
            @Schema(description = "게시물 파일 식별 ID")
            private Long boardFileId;
            @Schema(description = "이미지 URL")
            private String url;
        }

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
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
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Condition {
            @Schema(description = "고유 식별 ID")
            private Long conditionId;
            @Schema(description = "이름")
            private String name;
        }

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Compatibility {
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
                private String percent;
            }
        }
    }
}
