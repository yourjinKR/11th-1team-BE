package org.example.knockin.service.impl;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.knockin.dto.ChatRoomDetailDto;
import org.example.knockin.dto.MessageType;
import org.example.knockin.entity.chat.ChatRoomMessage;
import org.example.knockin.entity.chat.ChattingRoom;
import org.example.knockin.entity.member.Member;
import org.example.knockin.repository.chat.ChatRoomMessageRepository;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatRoomMessageServiceImpl {

    private final ChatRoomMessageRepository chatRoomMessageRepository;

    public ChatRoomMessage save(String contents, @Nullable Member member, ChattingRoom chattingRoom, MessageType type) {
        ChatRoomMessage chatRoomMessage = ChatRoomMessage.builder()
                .contents(contents)
                .member(member)
                .chattingRoom(chattingRoom)
                .type(type)
                .build();

        return chatRoomMessageRepository.save(chatRoomMessage);
    }

    public List<ChatRoomDetailDto.ChatMessage> findChatMessageDto(Long chatRoomId) {
        return chatRoomMessageRepository.findChatMessageDto(chatRoomId);
    }
}
