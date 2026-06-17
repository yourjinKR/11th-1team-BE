package org.example.knockin.entity.alarm;

import jakarta.persistence.*;
import lombok.*;
import org.example.knockin.entity.member.Member;
import org.hibernate.annotations.ColumnDefault;


@Getter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "alarm_setting")
public class AlarmSetting {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ColumnDefault("true")
    @Column(name = "is_enabled")
    private Boolean isEnabled;

    @Enumerated(EnumType.STRING)
    private AlarmType alarmType;

    public void updateEnable(Boolean isEnabled) {
        this.isEnabled = isEnabled;
    }
}
