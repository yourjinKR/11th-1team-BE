package org.example.knockin.repository.board.row;

import java.time.LocalDateTime;

public record BoardBaseRow(
        Long boardId,
        String title,
        Integer deposit,
        Integer monthlyRent,
        Integer managementCost,
        LocalDateTime comeableDate,
        Long hits,
        String roomTypeName,
        String regionName,
        String parentRegionName,
        String grandParentRegionName,
        Long memberId,
        String memberName
) {
}
