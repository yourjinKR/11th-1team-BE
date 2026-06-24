package org.example.knockin.repository.member;

import java.util.List;
import java.util.Optional;
import org.example.knockin.entity.member.BasicInformation;
import org.example.knockin.entity.member.Member;
import org.example.knockin.repository.member.row.ChattingRoomBasicInfoRow;

public interface BasicInformationRepositoryCustom {
    boolean isExsitBasicInformation(Member member);

    Optional<BasicInformation> findLatestBasicInformation(Member member);

    Optional<ChattingRoomBasicInfoRow> findChattingRoomBasicInfoRow(Member memberEntity);

    List<ChattingRoomBasicInfoRow> findChattingRoomBasicInfoRows(List<Long> memberIds);
}
