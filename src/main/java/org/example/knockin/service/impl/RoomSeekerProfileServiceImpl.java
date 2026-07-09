package org.example.knockin.service.impl;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.knockin.repository.room.RoomSeekerProfileRepository;
import org.example.knockin.repository.room.row.MatchingSeekerProfileRow;
import org.example.knockin.repository.room.row.MatchingSeekerRegionRow;
import org.example.knockin.repository.room.row.MatchingSeekerRoomTypeRow;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoomSeekerProfileServiceImpl {

    private final RoomSeekerProfileRepository roomSeekerProfileRepository;

    public List<MatchingSeekerProfileRow> findAllSeekerProfileByMemberIdIn(List<Long> memberIds) {
        return roomSeekerProfileRepository.findAllSeekerProfileByMemberIdIn(memberIds);
    }

    public List<MatchingSeekerRegionRow> findAllSeekerRegionByMemberIdIn(List<Long> memberIds) {
        return roomSeekerProfileRepository.findAllSeekerRegionByMemberIdIn(memberIds);
    }

    public List<MatchingSeekerRoomTypeRow> findAllSeekerRoomTypeByMemberIdIn(List<Long> memberIds) {
        return roomSeekerProfileRepository.findAllSeekerRoomTypeByMemberIdIn(memberIds);
    }
}
