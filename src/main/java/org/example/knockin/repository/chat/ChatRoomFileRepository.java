package org.example.knockin.repository.chat;

import org.example.knockin.entity.chat.ChatRoomFile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatRoomFileRepository extends JpaRepository<ChatRoomFile, Long> {
}
