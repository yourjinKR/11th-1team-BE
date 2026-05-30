package org.example.knockin.entity.chat;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.knockin.entity.member.Member;
import org.hibernate.annotations.ColumnDefault;


@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "chat_room_member")
public class ChatRoomMember {
    @EmbeddedId
    private ChatRoomMemberId id;

    @MapsId("chattingRoomId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chatting_room_id", nullable = false)
    private ChattingRoom chattingRoom;

    @MapsId("memberId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ColumnDefault("false")
    @Column(name = "is_left", nullable = false)
    private Boolean isLeft;
}
