package org.example.knockin.repository.chat;

import org.example.knockin.entity.chat.ChatRoomMessage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatRoomMessageRepository extends JpaRepository<ChatRoomMessage, Long>, ChatRoomMessageRepositoryCustom {
}
