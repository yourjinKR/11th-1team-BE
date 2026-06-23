package org.example.knockin.entity.room;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.knockin.entity.chat.ChattingRoom;
import org.example.knockin.entity.member.Member;
import org.example.knockin.global.jpa.BaseEntity;


@Getter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "roommate_matching_required")
public class RoommateMatchingRequired extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_id", nullable = false)
    private Member requester;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requestee_id", nullable = false)
    private Member requestee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chatting_room_id", nullable = false)
    private ChattingRoom chattingRoom;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private RoommateRequiredStatus status;

    public boolean isRequester(Long requesterId) {
        return this.requester.getId().equals(requesterId);
    }

    public boolean isRequestee(Long requesteeId) {
        return requestee.getId().equals(requesteeId);
    }

    public boolean isPending() {
        return this.status.equals(RoommateRequiredStatus.PENDING);
    }

    public void accept() {
        this.status = RoommateRequiredStatus.ACCEPTED;
    }

    public void reject() {
        this.status = RoommateRequiredStatus.REJECTED;
    }

    public void cancel() {
        this.status = RoommateRequiredStatus.CANCELED;
    }

    public void expire() {
        this.status = RoommateRequiredStatus.EXPIRED;
    }
}
