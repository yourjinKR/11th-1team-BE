package org.example.knockin.repository.board.impl;

import static org.example.knockin.entity.board.QRoommateBoardOption.roommateBoardOption;
import static org.example.knockin.entity.room.QRoomExtraOption.roomExtraOption;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.knockin.dto.BoardEditDto;
import org.example.knockin.entity.board.RoommateBoardOption;
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

    @Override
    public List<BoardEditDto.Response.BoardOptionInfo> getExtraOptionsByBoardId(Long boardId) {
        return jpaQueryFactory
                .select(Projections.constructor(
                        BoardEditDto.Response.BoardOptionInfo.class,
                        roomExtraOption.id,
                        roomExtraOption.name
                ))
                .from(roommateBoardOption)
                .join(roommateBoardOption.roomExtraOption, roomExtraOption)
                .where(
                        roommateBoardOption.roommateBoard.id.eq(boardId),
                        roomExtraOption.isDeleted.isFalse()
                )
                .fetch();
    }

    @Override
    public List<RoommateBoardOption> findWithRoomExtraOptionByBoardId(Long boardId) {
        return jpaQueryFactory
                .select(roommateBoardOption)
                .distinct()
                .from(roommateBoardOption)
                .join(roommateBoardOption.roomExtraOption).fetchJoin()
                .where(roommateBoardOption.roommateBoard.id.eq(boardId), roomExtraOption.isDeleted.isFalse())
                .fetch();
    }
}
