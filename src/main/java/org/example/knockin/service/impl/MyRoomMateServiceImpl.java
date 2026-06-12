package org.example.knockin.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.knockin.entity.member.Member;
import org.example.knockin.repository.room.MyRoommateRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MyRoomMateServiceImpl {
    private final MyRoommateRepository myRoommateRepository;

    public boolean isExistRoomMate(Member member) {
        return myRoommateRepository.isExistRoomMate(member);
    }
}
