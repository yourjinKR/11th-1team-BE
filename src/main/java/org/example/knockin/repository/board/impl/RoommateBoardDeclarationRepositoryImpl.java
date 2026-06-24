package org.example.knockin.repository.board.impl;

import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.example.knockin.dto.BoReportDoneListDto;
import org.example.knockin.dto.BoReportWaitListDto;
import org.example.knockin.global.jpa.DeclarationType;
import org.example.knockin.global.util.ReportType;
import org.example.knockin.repository.board.RoommateBoardDeclarationRepositoryCustom;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

import static org.example.knockin.entity.member.QMember.member;
import static org.example.knockin.entity.board.QRoommateBoardDeclaration.roommateBoardDeclaration;
import static org.example.knockin.entity.member.QBasicInformation.basicInformation;

@Repository
@RequiredArgsConstructor
public class RoommateBoardDeclarationRepositoryImpl implements RoommateBoardDeclarationRepositoryCustom {
    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<BoReportWaitListDto.Response.ReportInfo> findReportWaitList(Pageable pageable) {
        return jpaQueryFactory
                .select(Projections.fields(BoReportWaitListDto.Response.ReportInfo.class,
                        roommateBoardDeclaration.id,
                        ExpressionUtils.as(Expressions.constant(ReportType.BOARD.name()), "type"),
                        basicInformation.name.as("reporter"),
                        roommateBoardDeclaration.member.id.as("reporterId"),
                        roommateBoardDeclaration.roommateBoard.id.as("reportedId"),
                        roommateBoardDeclaration.reason,
                        roommateBoardDeclaration.createdAt
                ))
                .from(roommateBoardDeclaration)
                .join(roommateBoardDeclaration.member, member)
                .leftJoin(basicInformation)
                .on(basicInformation.member.eq(roommateBoardDeclaration.member))
                .where(roommateBoardDeclaration.declarationType.eq(DeclarationType.PENDING))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
    }

    @Override
    public List<BoReportDoneListDto.Response.ReportInfo> findReportDoneList(Pageable pageable) {
        return jpaQueryFactory
                .select(Projections.fields(BoReportDoneListDto.Response.ReportInfo.class,
                        roommateBoardDeclaration.id,
                        ExpressionUtils.as(Expressions.constant(ReportType.BOARD.name()), "type"),
                        basicInformation.name.as("reporter"),
                        roommateBoardDeclaration.member.id.as("reporterId"),
                        roommateBoardDeclaration.roommateBoard.id.as("reportedId"),
                        roommateBoardDeclaration.reason,
                        roommateBoardDeclaration.createdAt
                ))
                .from(roommateBoardDeclaration)
                .join(roommateBoardDeclaration.member, member)
                .leftJoin(basicInformation)
                .on(basicInformation.member.eq(roommateBoardDeclaration.member))
                .where(roommateBoardDeclaration.declarationType.ne(DeclarationType.PENDING))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
    }
}