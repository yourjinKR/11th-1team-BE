package org.example.knockin.entity.chat;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.knockin.entity.member.Member;
import org.hibernate.annotations.ColumnDefault;


@Getter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "chat_room_member",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_chat_room_member_chatting_room_member",
                columnNames = {"chatting_room_id", "member_id"}
        )
)
public class ChatRoomMember {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chatting_room_id", nullable = false)
    private ChattingRoom chattingRoom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Builder.Default
    @ColumnDefault("false")
    @Column(name = "is_left", nullable = false)
    private Boolean isLeft = false;

    public static ChatRoomMember of(ChattingRoom chattingRoom, Member member) {
        return ChatRoomMember.builder()
                .chattingRoom(chattingRoom)
                .member(member)
                .isLeft(false)
                .build();
    }

    public void left() {
        isLeft = true;
    }
}
