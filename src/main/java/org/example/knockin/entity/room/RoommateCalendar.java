package org.example.knockin.entity.room;

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
import org.example.knockin.global.jpa.CreatedAtEntity;
import org.hibernate.annotations.ColumnDefault;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "roommate_calendar")
public class RoommateCalendar extends CreatedAtEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "my_roommate_id", nullable = false)
    private MyRoommate myRoommate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "roommate_calendar_type_id", nullable = false)
    private RoommateCalendarType roommateCalendarType;

    @Column(name = "title", nullable = false, length = 50)
    private String title;

    @Column(name = "contents", length = 500)
    private String contents;

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;

    @ColumnDefault("false")
    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted;
}
