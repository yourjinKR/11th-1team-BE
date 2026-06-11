package org.example.knockin.repository.board.row;

import java.time.LocalDateTime;

public record EditFormRow(
        String title,
        Integer deposit,
        Integer monthlyRent,
        Integer managementCost,
        Long roomTypeId,
        String roomTypeName,
        Long regionId,
        String regionName,
        String parentRegionName,
        String grandParentRegionName,
        LocalDateTime comeableDate,
        String contents
) {
}
