package org.example.knockin.repository.member.row;

import java.time.LocalDate;
import org.example.knockin.entity.member.Gender;
import org.example.knockin.entity.room.RoomProfileType;
import org.example.knockin.global.util.HasMemberId;

public record MatchingBasicInfoRow(
        Long memberId,
        String memberProfileImageUrl,
        String memberName,
        LocalDate birth,
        Gender gender,
        Long roomProfileId,
        RoomProfileType roomProfileType
) implements HasMemberId {
}
