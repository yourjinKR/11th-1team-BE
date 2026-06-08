package org.example.knockin.repository.life;

import org.example.knockin.entity.life.PreferenceConditionLog;
import org.example.knockin.entity.life.PreferenceConditionWeightLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PreferenceConditionWeightLogRepository extends JpaRepository<PreferenceConditionWeightLog, Long> {
}