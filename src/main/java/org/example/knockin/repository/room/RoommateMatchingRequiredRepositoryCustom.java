package org.example.knockin.repository.room;

import java.util.List;
import java.util.Optional;
import org.example.knockin.dto.RoommateRequestDto.RoommateMatchingRequiredInfo;
import org.example.knockin.entity.chat.ChattingRoom;
import org.example.knockin.entity.room.RoommateMatchingRequired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface RoommateMatchingRequiredRepositoryCustom {
    Optional<RoommateMatchingRequired> findLatest(Long chattingRoomId);

    List<RoommateMatchingRequiredInfo> findRequiredDto(ChattingRoom chattingRoomEntity);

    Page<RoommateMatchingRequired> findMyRequiredList(Long memberId, Pageable pageable);
}
