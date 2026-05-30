package org.example.knockin.entity.board;

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
import org.example.knockin.entity.room.Region;
import org.example.knockin.entity.room.RoomType;
import org.example.knockin.global.jpa.BaseEntity;
import org.hibernate.annotations.ColumnDefault;


@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "roommate_board")
public class RoommateBoard extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "title", length = 50)
    private String title;

    @Column(name = "contents", length = 500)
    private String contents;

    @Column(name = "deposit")
    private Integer deposit;

    @Column(name = "monthly_rent")
    private Integer monthlyRent;

    @Column(name = "management_cost")
    private Integer managementCost;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_type", nullable = false)
    private RoomType roomType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region_id", nullable = false)
    private Region region;

    @Column(name = "comeable_date", nullable = false)
    private LocalDateTime comeableDate;

    @ColumnDefault("false")
    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted;

    @ColumnDefault("0")
    @Column(name = "hits", nullable = false)
    private Long hits;
}
