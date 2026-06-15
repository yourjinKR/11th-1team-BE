package org.example.knockin.entity.board;

import jakarta.persistence.*;
import lombok.*;
import org.example.knockin.entity.member.Member;
import org.example.knockin.global.jpa.BaseEntity;
import org.hibernate.annotations.ColumnDefault;

@Getter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "faq")
public class Faq extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false, length = 50)
    private String title;

    @Column(nullable = false, length = 500)
    private String contents;

    @ManyToOne
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ColumnDefault(value = "false")
    private Boolean isDeleted;

    @Column(name = "sort", nullable = false)
    private Integer sort;
}
