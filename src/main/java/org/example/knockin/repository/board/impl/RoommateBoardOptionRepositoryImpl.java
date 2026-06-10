package org.example.knockin.repository.board.impl;

import static org.example.knockin.entity.board.QRoommateBoardOption.roommateBoardOption;
import static org.example.knockin.entity.room.QRoomExtraOption.roomExtraOption;

import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.knockin.repository.board.RoommateBoardOptionRepositoryCustom;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class RoommateBoardOptionRepositoryImpl implements RoommateBoardOptionRepositoryCustom {
    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<String> getExtraOptionsNameByBoardId(Long boardId) {
        return jpaQueryFactory
                .select(roomExtraOption.name)
                .from(roommateBoardOption)
                .join(roommateBoardOption.roomExtraOption, roomExtraOption)
                .where(
                        roommateBoardOption.roommateBoard.id.eq(boardId),
                        roomExtraOption.isDeleted.isFalse()
                )
                .fetch();
    }
}
