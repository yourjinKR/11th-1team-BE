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
import lombok.*;
import org.example.knockin.dto.BoNoticeDto;
import org.example.knockin.entity.member.Member;
import org.example.knockin.global.entity.BaseEntity;
import org.hibernate.annotations.ColumnDefault;


@Getter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "notification")
public class Notification extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "title", length = 50)
    private String title;

    @Column(name = "contents", columnDefinition = "TEXT")
    private String contents;

    @Builder.Default
    @ColumnDefault("false")
    @Column(name = "is_deleted")
    private Boolean isDeleted = false;

    public void modifyNotification(BoNoticeDto.Request request, Member member) {
        this.title = request.getTitle();
        this.contents = request.getContents();
        this.member = member;
    }

    public void deleteNotification(Member member) {
        this.member = member;
        this.isDeleted = true;
    }
}
