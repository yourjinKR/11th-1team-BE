package org.example.knockin.repository.chat.impl;

import static org.example.knockin.entity.chat.QChattingRequired.chattingRequired;
import static org.example.knockin.entity.chat.QChattingRoom.chattingRoom;
import static org.example.knockin.entity.chat.QChatRoomMessage.chatRoomMessage;
import static org.example.knockin.entity.file.QBasicInformationFile.basicInformationFile;
import static org.example.knockin.entity.member.QBasicInformation.basicInformation;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.knockin.dto.ChatRoomListDto;
import org.example.knockin.entity.chat.QChatRoomMember;
import org.example.knockin.entity.file.QBasicInformationFile;
import org.example.knockin.entity.member.QBasicInformation;
import org.example.knockin.entity.member.QMember;
import org.example.knockin.repository.chat.ChattingRoomRepositoryCustom;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ChattingRoomRepositoryImpl implements ChattingRoomRepositoryCustom {
    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<ChatRoomListDto.Response> findByMemberId(Long memberId) {
        QMember viewerMember = new QMember("viewerMember");
        QMember opponentMember = new QMember("opponentMember");
        QChatRoomMember viewerRoomMember = new QChatRoomMember("viewerRoomMember");
        QChatRoomMember opponentRoomMember = new QChatRoomMember("opponentRoomMember");
        QBasicInformation basicInformationSub = new QBasicInformation("basicInformationSub");
        QBasicInformationFile basicInformationFileSub = new QBasicInformationFile("basicInformationFileSub");

        return jpaQueryFactory
                .select(Projections.constructor(
                        ChatRoomListDto.Response.class,
                        chattingRoom.id,
                        basicInformation.name,
                        basicInformationFile.file.savedFileName,
                        chattingRoom.createdAt,
                        chattingRoom.chattingRequired.status,
                        chatRoomMessage.contents
                ))
                .from(chattingRoom)
                .join(chattingRoom.chattingRequired, chattingRequired)
                .join(viewerRoomMember)
                .on(
                        viewerRoomMember.chattingRoom.eq(chattingRoom),
                        viewerRoomMember.member.id.eq(memberId),
                        viewerRoomMember.isLeft.isFalse()
                )
                .join(viewerRoomMember.member, viewerMember)
                .join(opponentRoomMember)
                .on(
                        opponentRoomMember.chattingRoom.eq(chattingRoom),
                        opponentRoomMember.member.id.ne(memberId)
                )
                .join(opponentRoomMember.member, opponentMember)
                .leftJoin(basicInformation)
                .on(basicInformation.id.eq(
                        JPAExpressions
                                .select(basicInformationSub.id.max())
                                .from(basicInformationSub)
                                .where(basicInformationSub.member.eq(opponentMember))
                ))
                .leftJoin(basicInformationFile)
                .on(basicInformationFile.id.eq(
                        JPAExpressions
                                .select(basicInformationFileSub.id.max())
                                .from(basicInformationFileSub)
                                .where(basicInformationFileSub.basicInformation.eq(basicInformation))
                ))
                .leftJoin(chatRoomMessage)
                .on(chatRoomMessage.id.eq(
                        JPAExpressions
                                .select(chatRoomMessage.id.max())
                                .from(chatRoomMessage)
                                .where(chatRoomMessage.chattingRoom.eq(chattingRoom))
                ))
                .orderBy(chattingRoom.createdAt.desc())
                .fetch();
    }
}
