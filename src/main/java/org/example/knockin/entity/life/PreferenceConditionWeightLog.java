package org.example.knockin.entity.life;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "preference_condition_weight_log")
public class PreferenceConditionWeightLog {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "preference_condition_log_id")
    private PreferenceConditionLog preferenceConditionLog;

    @ManyToOne
    @JoinColumn(name = "life_pattern_id")
    private LifePattern lifePattern;
}
