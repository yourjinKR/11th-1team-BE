package org.example.knockin.service.impl;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.knockin.entity.chat.ChattingScore;
import org.example.knockin.repository.chat.ChattingScoreRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChattingScoreServiceImpl {

    private final ChattingScoreRepository chattingScoreRepository;

    public List<ChattingScore> saveAll(List<ChattingScore> chattingScores) {
        return chattingScoreRepository.saveAll(chattingScores);
    }
}
