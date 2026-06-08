package org.example.knockin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import java.util.List;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.example.knockin.entity.auth.AuthenticationType;
import org.example.knockin.entity.board.RoommateBoardBadgeType;
import org.example.knockin.entity.member.Gender;

@Data
public class BoardListDto {
    @Data
    public static class Request {
        @Schema(description = "지역 ids")
        List<Long> regionIds;

        @Schema(description = "방 형태 ids")
        List<Long> roomTypeIds;

        @Schema(description = "성별")
        Gender gender;

        @Schema(description = "최소 보증금")
        Integer minDeposit;

        @Schema(description = "최대 보증금")
        Integer maxDeposit;

        @Schema(description = "최대 월세")
        Integer minMounthRent;

        @Schema(description = "최소 월세")
        Integer maxMounthRent;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        Long id;
        String imageUrl;
        String title;
        Integer deposit;
        Integer monthlyRent;
        Integer managementCost;
        List<String> roomTypes;
        LocalDateTime comeableDate;
        String regionFullName;
        String memberName;
        List<AuthenticationType> authentications;
        Long hits;
        List<RoommateBoardBadgeType> badges;
    }
}
