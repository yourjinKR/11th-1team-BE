package org.example.knockin.repository.chat;

import java.util.List;
import org.example.knockin.dto.ChatRoomDetailDto.ChatMessage;

public interface ChatRoomMessageRepositoryCustom {
    List<ChatMessage> findChatMessageDto(Long chatRoomId);
}
