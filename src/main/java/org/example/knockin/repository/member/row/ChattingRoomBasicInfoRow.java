package org.example.knockin.repository.member.row;

import java.time.LocalDate;
import org.example.knockin.entity.member.Gender;
import org.example.knockin.global.util.HasMemberId;

public record ChattingRoomBasicInfoRow(
        Long memberId,
        String name,
        LocalDate birth,
        Gender gender,
        String profileImageUrl
) implements HasMemberId {
}
