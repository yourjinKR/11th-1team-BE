package org.example.knockin.entity.chat;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Embeddable
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoomMemberId implements Serializable {
    @Column(name = "chatting_room_id")
    private Long chattingRoomId;

    @Column(name = "member_id")
    private Long memberId;
}
