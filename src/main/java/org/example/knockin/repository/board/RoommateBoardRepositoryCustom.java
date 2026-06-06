package org.example.knockin.repository.board;

import jakarta.annotation.Nullable;
import org.example.knockin.entity.board.RoommateBoard;
import org.example.knockin.entity.member.Gender;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface RoommateBoardRepositoryCustom {
    Page<RoommateBoard> search(
            @Nullable Long regionId,
            @Nullable Long roomTypeId,
            @Nullable Gender gender,
            @Nullable Integer minDeposit,
            @Nullable Integer maxDeposit,
            @Nullable Integer minMounthRent,
            @Nullable Integer maxMounthRent,
            Pageable pageable
    );
}
