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
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.knockin.entity.room.RoomExtraOption;


@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "roommate_board_option")
public class RoommateBoardOption {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "roommate_board_id", nullable = false)
    private RoommateBoard roommateBoard;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_extra_option_id", nullable = false)
    private RoomExtraOption roomExtraOption;
}
