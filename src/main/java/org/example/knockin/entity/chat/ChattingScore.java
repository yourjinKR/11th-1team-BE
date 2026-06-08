package org.example.knockin.entity.chat;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.knockin.entity.life.MemberLifePatternLog;
import org.example.knockin.entity.life.PreferenceConditionLog;
import org.example.knockin.entity.life.PreferenceConditionWeightLog;


@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "chatting_score")
public class ChattingScore {
    @EmbeddedId
    private ChattingScoreId id;

    @MapsId("chattingRequiredId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chatting_required_id", nullable = false)
    private ChattingRequired chattingRequired;

    @MapsId("preferenceConditionLogId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "preference_condition_log_id", nullable = false)
    private PreferenceConditionLog preferenceConditionLog;

    @MapsId("lifePatternInformationLogId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "life_pattern_information_log_id", nullable = false)
    private MemberLifePatternLog lifePatternInformationLog;

    @MapsId("preferenceConditionWeightLog")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "preference_condition_weight_log_id", nullable = false)
    private PreferenceConditionWeightLog preferenceConditionWeightLog;

    @Column(name = "score", nullable = false)
    private Integer score;
}
