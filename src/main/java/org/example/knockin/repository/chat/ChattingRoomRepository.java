package org.example.knockin.repository.chat;

import org.example.knockin.entity.chat.ChattingRoom;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChattingRoomRepository extends JpaRepository<ChattingRoom, Long> {
}