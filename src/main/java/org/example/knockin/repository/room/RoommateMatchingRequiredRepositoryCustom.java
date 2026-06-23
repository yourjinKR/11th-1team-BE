package org.example.knockin.repository.room;

import java.util.List;
import java.util.Optional;
import org.example.knockin.dto.RoommateRequestDto.RoommateMatchingRequiredInfo;
import org.example.knockin.entity.chat.ChattingRoom;
import org.example.knockin.entity.room.RoommateMatchingRequired;

public interface RoommateMatchingRequiredRepositoryCustom {
    Optional<RoommateMatchingRequired> findLatest(Long chattingRoomId);

    List<RoommateMatchingRequiredInfo> findRequiredDto(ChattingRoom chattingRoomEntity);
}
