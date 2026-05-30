package org.example.knockin.entity.chat;

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
public class ChattingScoreId implements Serializable {
    @Column(name = "chatting_required_id")
    private Long chattingRequiredId;

    @Column(name = "preference_condition_log_id")
    private Long preferenceConditionLogId;

    @Column(name = "life_pattern_information_log_id")
    private Long lifePatternInformationLogId;
}
