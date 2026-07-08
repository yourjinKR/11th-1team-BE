package org.example.knockin.service.impl;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.knockin.entity.room.RoommateScore;
import org.example.knockin.repository.room.RoommateScoreRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MyRoommateScoreServiceImpl {

    private final RoommateScoreRepository roommateScoreRepository;

    public List<RoommateScore> findByRoommateId(Long myRoommateId) {
        return roommateScoreRepository.findWithScoreDetailsByMyRoommateId(myRoommateId);
    }
}
