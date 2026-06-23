package org.example.knockin.repository.chat.impl;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import static org.example.knockin.entity.chat.QChatRoomMessage.chatRoomMessage;
import static org.example.knockin.entity.chat.QChatRoomFile.chatRoomFile;
import static org.example.knockin.entity.file.QFile.file;


import org.example.knockin.dto.ChatRoomDetailDto.ChatMessage;
import org.example.knockin.repository.chat.ChatRoomMessageRepositoryCustom;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ChatRoomMessageRepositoryImpl implements ChatRoomMessageRepositoryCustom {
    public final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<ChatMessage> findChatMessageDto(Long chatRoomId) {
        return jpaQueryFactory
                .select(Projections.constructor(
                        ChatMessage.class,
                        chatRoomMessage.id,
                        chatRoomMessage.member.id,
                        chatRoomMessage.contents,
                        chatRoomMessage.createdAt,
                        chatRoomMessage.type,
                        file.savedFileName
                ))
                .from(chatRoomMessage)
                .leftJoin(chatRoomFile)
                .on(chatRoomFile.chatRoomMessage.eq(chatRoomMessage))
                .leftJoin(chatRoomFile.file, file)
                .where(chatRoomMessage.chattingRoom.id.eq(chatRoomId))
                .orderBy(chatRoomMessage.createdAt.asc(), chatRoomMessage.id.asc())
                .fetch();
    }
}
