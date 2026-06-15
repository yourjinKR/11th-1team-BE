package org.example.knockin.entity.payment;


import jakarta.persistence.*;
import lombok.*;
import org.example.knockin.entity.member.Member;
import org.example.knockin.global.jpa.CreatedAtEntity;

@Getter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "point_log")
public class PointLog extends CreatedAtEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private Long point;

    @Column(length = 50)
    private String reason;

    @Enumerated(EnumType.STRING)
    private VarianceType variance;

    @ManyToOne
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;
}
