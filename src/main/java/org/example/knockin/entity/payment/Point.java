package org.example.knockin.entity.payment;

import jakarta.persistence.*;
import lombok.*;
import org.example.knockin.entity.member.Member;

@Getter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "point")
public class Point {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private Long points;

    @ManyToOne
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;
}
