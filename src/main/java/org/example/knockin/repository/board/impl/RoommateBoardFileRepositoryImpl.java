package org.example.knockin.repository.board.impl;

import static org.example.knockin.entity.board.QRoommateBoardFile.roommateBoardFile;
import static org.example.knockin.entity.file.QFile.file;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.knockin.dto.BoardDetailDto.Response.FileDetailDto;
import org.example.knockin.repository.board.RoommateBoardFileRepositoryCustom;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class RoommateBoardFileRepositoryImpl implements RoommateBoardFileRepositoryCustom {
    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<FileDetailDto> getFileDetailDtoByBoardId(Long boardId) {
        return jpaQueryFactory
                .select(Projections.constructor(
                        FileDetailDto.class,
                        roommateBoardFile.id,
                        file.savedFileName
                ))
                .from(roommateBoardFile)
                .join(roommateBoardFile.file, file)
                .where(
                        roommateBoardFile.roommateBoard.id.eq(boardId),
                        file.isDeleted.isFalse()
                )
                .orderBy(roommateBoardFile.isThumbnail.desc(), roommateBoardFile.id.asc())
                .limit(10)
                .fetch();
    }
}
