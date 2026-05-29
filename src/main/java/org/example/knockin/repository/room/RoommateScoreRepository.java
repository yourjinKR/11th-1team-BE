package org.example.knockin.repository.room;

import org.example.knockin.entity.room.RoommateScore;
import org.example.knockin.entity.room.RoommateScoreId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoommateScoreRepository extends JpaRepository<RoommateScore, RoommateScoreId> {
}