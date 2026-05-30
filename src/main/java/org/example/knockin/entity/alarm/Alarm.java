package org.example.knockin.entity.alarm;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.knockin.entity.member.Member;
import org.example.knockin.global.jpa.CreatedAtEntity;
import org.hibernate.annotations.ColumnDefault;


@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "alarm")
public class Alarm extends CreatedAtEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "title", nullable = false, length = 50)
    private String title;

    @Column(name = "contents", length = 500)
    private String contents;

    @ColumnDefault("false")
    @Column(name = "is_read", nullable = false)
    private Boolean isRead;

    @Column(name = "expired_at", nullable = false)
    private LocalDateTime expiredAt;
}
