package org.example.knockin.repository.board;

import jakarta.annotation.Nullable;
import java.time.LocalDateTime;
import java.util.List;
import org.example.knockin.entity.member.Gender;
import org.springframework.data.domain.Pageable;

public record RoommateBoardSearchCondition(
        @Nullable List<Long> regionIds,
        @Nullable List<Long> roomTypeIds,
        @Nullable Gender gender,
        @Nullable Integer minDeposit,
        @Nullable Integer maxDeposit,
        @Nullable Integer minMounthRent,
        @Nullable Integer maxMounthRent,
        LocalDateTime endDate,
        Pageable pageable
) {
}
