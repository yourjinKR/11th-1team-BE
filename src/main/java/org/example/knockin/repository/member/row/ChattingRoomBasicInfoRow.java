package org.example.knockin.repository.member.row;

import java.time.LocalDate;
import org.example.knockin.entity.member.Gender;

public record ChattingRoomBasicInfoRow(
        String name,
        LocalDate birth,
        Gender gender,
        String profileImageUrl
) {
}
