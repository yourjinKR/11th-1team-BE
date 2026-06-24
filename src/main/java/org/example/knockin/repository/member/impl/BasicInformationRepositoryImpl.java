package org.example.knockin.repository.member.impl;

import static org.example.knockin.entity.file.QBasicInformationFile.basicInformationFile;
import static org.example.knockin.entity.file.QFile.file;
import static org.example.knockin.entity.member.QBasicInformation.basicInformation;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.example.knockin.entity.file.QBasicInformationFile;
import org.example.knockin.entity.member.QBasicInformation;
import org.example.knockin.entity.member.BasicInformation;
import org.example.knockin.entity.member.Member;
import org.example.knockin.repository.member.BasicInformationRepositoryCustom;
import org.example.knockin.repository.member.row.ChattingRoomBasicInfoRow;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class BasicInformationRepositoryImpl implements BasicInformationRepositoryCustom {
    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public boolean isExsitBasicInformation(Member member) {
        Long result = jpaQueryFactory.select(basicInformation.id).from(basicInformation).where(basicInformation.member.eq(member)).fetchFirst();
        return result != null;
    }

    @Override
    public Optional<BasicInformation> findLatestBasicInformation(Member member) {
        return Optional.ofNullable(jpaQueryFactory
                .selectFrom(basicInformation)
                .where(basicInformation.member.eq(member))
                .orderBy(basicInformation.id.desc())
                .fetchFirst()
        );
    }

    @Override
    public Optional<ChattingRoomBasicInfoRow> findChattingRoomBasicInfoRow(Member memberEntity) {
        QBasicInformationFile latestBasicInformationFile = new QBasicInformationFile("latestBasicInformationFile");

        return Optional.ofNullable(jpaQueryFactory
                .select(Projections.constructor(
                        ChattingRoomBasicInfoRow.class,
                        basicInformation.member.id,
                        basicInformation.name,
                        basicInformation.birth,
                        basicInformation.gender,
                        file.savedFileName
                ))
                .from(basicInformation)
                .leftJoin(basicInformationFile)
                .on(basicInformationFile.id.eq(
                        JPAExpressions
                                .select(latestBasicInformationFile.id.max())
                                .from(latestBasicInformationFile)
                                .where(latestBasicInformationFile.basicInformation.id.eq(basicInformation.id))
                ))
                .leftJoin(basicInformationFile.file, file)
                .where(basicInformation.member.eq(memberEntity))
                .orderBy(basicInformation.id.desc())
                .fetchFirst()
        );
    }

    @Override
    public List<ChattingRoomBasicInfoRow> findChattingRoomBasicInfoRows(List<Long> memberIds) {
        if (memberIds == null || memberIds.isEmpty()) return List.of();
        QBasicInformation latestBasicInformation = new QBasicInformation("latestBasicInformation");
        QBasicInformationFile latestBasicInformationFile = new QBasicInformationFile("latestBasicInformationFile");

        return jpaQueryFactory
                .select(Projections.constructor(
                        ChattingRoomBasicInfoRow.class,
                        basicInformation.member.id,
                        basicInformation.name,
                        basicInformation.birth,
                        basicInformation.gender,
                        file.savedFileName
                ))
                .from(basicInformation)
                .leftJoin(basicInformationFile)
                .on(basicInformationFile.id.eq(
                        JPAExpressions
                                .select(latestBasicInformationFile.id.max())
                                .from(latestBasicInformationFile)
                                .where(latestBasicInformationFile.basicInformation.id.eq(basicInformation.id))
                ))
                .leftJoin(basicInformationFile.file, file)
                .where(
                        basicInformation.member.id.in(memberIds),
                        basicInformation.id.eq(
                                JPAExpressions
                                        .select(latestBasicInformation.id.max())
                                        .from(latestBasicInformation)
                                        .where(latestBasicInformation.member.id.eq(basicInformation.member.id))
                        )
                )
                .orderBy(basicInformation.id.desc())
                .fetch();
    }
}
