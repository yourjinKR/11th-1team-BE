package org.example.knockin.entity.payment;

import jakarta.persistence.*;
import lombok.*;
import org.example.knockin.entity.member.Member;
import org.example.knockin.global.entity.CreatedAtEntity;

@Getter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "payment")
public class Payment extends CreatedAtEntity {
    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String paymentKey;

    @Enumerated(EnumType.STRING)
    private PaymentType type;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    private Long amount;

    @ManyToOne
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;
}
