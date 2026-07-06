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
import java.util.Objects;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.knockin.dto.CalendarDto;
import org.example.knockin.entity.member.Member;
import org.example.knockin.global.jpa.BaseEntity;
import org.hibernate.annotations.ColumnDefault;

@Getter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "roommate_calendar")
public class RoommateCalendar extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "my_roommate_id", nullable = false)
    private MyRoommate myRoommate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false, comment = "작성자")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "roommate_calendar_category_id", nullable = false)
    private RoommateCalendarCategory roommateCalendarCategory;

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

    public void modify(CalendarDto.CalendarInfoDto calendarInfo) {
        this.title = calendarInfo.getTitle();
        this.contents = calendarInfo.getContents();
        this.startDate = calendarInfo.getStartDate();
        this.endDate = calendarInfo.getEndDate();
    }

    public boolean isOwner(Long memberId) {
        return Objects.equals(this.member.getId(), memberId);
    }

    public void softDelete() {
        this.isDeleted = true;
    }
}
