package org.example.knockin.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.knockin.entity.member.Member;
import org.example.knockin.entity.room.RoomOfferProfile;
import org.example.knockin.entity.room.RoomProfile;
import org.example.knockin.entity.room.RoomSeekerProfile;
import org.example.knockin.repository.room.RoomProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoomProfileServiceImpl {
    private final RoomProfileRepository roomProfileRepository;

    @Transactional
    public RoomOfferProfile save(RoomOfferProfile roomOfferProfile) {
        return roomProfileRepository.save(roomOfferProfile);
    }

    @Transactional
    public RoomSeekerProfile save(RoomSeekerProfile roomSeekerProfile) {
        return roomProfileRepository.save(roomSeekerProfile);
    }

    public List<RoomProfile> findByMember(Member member) {
        return roomProfileRepository.findByMember(member);
    }

    @Transactional
    public RoomProfile delete(RoomProfile roomProfile) {
        roomProfileRepository.delete(roomProfile);
        roomProfileRepository.flush();
        return roomProfile;
    }
}
