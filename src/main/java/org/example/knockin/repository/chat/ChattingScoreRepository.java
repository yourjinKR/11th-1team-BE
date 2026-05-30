package org.example.knockin.repository.chat;

import org.example.knockin.entity.chat.ChattingScore;
import org.example.knockin.entity.chat.ChattingScoreId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChattingScoreRepository extends JpaRepository<ChattingScore, ChattingScoreId> {
}