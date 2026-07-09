package org.example.knockin.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.knockin.entity.chat.ChatRoomFile;
import org.example.knockin.entity.chat.ChatRoomMessage;
import org.example.knockin.entity.file.File;
import org.example.knockin.repository.chat.ChatRoomFileRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatRoomFileServiceImpl {

    private final ChatRoomFileRepository chatRoomFileRepository;

    public ChatRoomFile save(File file, ChatRoomMessage chatRoomMessage) {
        ChatRoomFile chatRoomFile = ChatRoomFile.builder()
                .file(file)
                .chatRoomMessage(chatRoomMessage)
                .build();

        return chatRoomFileRepository.save(chatRoomFile);
    }
}
