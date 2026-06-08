package org.example.knockin.entity.room;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Embeddable
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class RoommateScoreId implements Serializable {
    @Column(name = "my_roommate_id")
    private Long myRoommateId;

    @Column(name = "preference_condition_log_id")
    private Long preferenceConditionLogId;

    @Column(name = "life_pattern_information_log_id")
    private Long lifePatternInformationLogId;

    @Column(name = "preference_condition_weight_log_id")
    private Long preferenceConditionWeightLog;
}
