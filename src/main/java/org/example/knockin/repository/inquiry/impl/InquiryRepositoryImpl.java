package org.example.knockin.repository.inquiry.impl;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.example.knockin.dto.InquiryDetailDto;
import org.example.knockin.dto.InquiryListDto;
import org.example.knockin.entity.member.Member;
import org.example.knockin.repository.inquiry.InquiryRepositoryCustom;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

import static org.example.knockin.entity.inquiry.QInquiry.inquiry;
import static org.example.knockin.entity.member.QBasicInformation.basicInformation;
import static org.example.knockin.entity.inquiry.QInquiryComment.inquiryComment;
import static com.querydsl.core.group.GroupBy.groupBy;
import static com.querydsl.core.group.GroupBy.list;

@Repository
@RequiredArgsConstructor
public class InquiryRepositoryImpl implements InquiryRepositoryCustom {
    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<InquiryListDto.Response.InquiryItem> findMyInquiryList(Boolean isDeleted, Member memberEntity, Pageable pageable) {
        return jpaQueryFactory.select(Projections.fields(InquiryListDto.Response.InquiryItem.class,
                inquiry.id,
                inquiry.title,
                inquiry.createdAt,
                inquiry.inquiryCategory.title.as("type"), basicInformation.name.as("writer"),
                new CaseBuilder().when(inquiryComment.id.isNull()).then("답변 대기중").otherwise("답변 완료").as("status")
                )).from(inquiry).leftJoin(inquiry.inquiryCategory)
                .leftJoin(inquiryComment).on(inquiryComment.inquiry.eq(inquiry))
                .leftJoin(basicInformation).on(basicInformation.member.eq(inquiry.member))
                .where(inquiry.isDeleted.eq(isDeleted), inquiry.member.eq(memberEntity))
                .offset(pageable.getOffset()).limit(pageable.getPageSize()).fetch();
    }

    @Override
    public InquiryDetailDto.Response.InquiryDetail findMyInquiry(Boolean isDeleted, Member memberEntity, Long inquiryId) {
        return jpaQueryFactory
                .from(inquiry)
                .leftJoin(inquiry.inquiryCategory)
                .leftJoin(inquiryComment).on(inquiryComment.inquiry.eq(inquiry))
                .leftJoin(basicInformation).on(basicInformation.member.eq(inquiry.member))
                .where(inquiry.id.eq(inquiryId), inquiry.isDeleted.eq(isDeleted), inquiry.member.eq(memberEntity))
                .transform(groupBy(inquiry.id).list(
                        Projections.fields(InquiryDetailDto.Response.InquiryDetail.class,
                                inquiry.id,
                                inquiry.title,
                                inquiry.contents,
                                basicInformation.name.as("writer"),
                                new CaseBuilder().when(inquiryComment.id.isNull()).then("답변 대기중").otherwise("답변 완료").as("status"),
                                inquiry.createdAt.as("createAt"),
                                inquiry.inquiryCategory.title.as("type"),
                                list(Projections.fields(InquiryDetailDto.Response.InquiryDetail.Reply.class,
                                        inquiryComment.id,
                                        inquiryComment.contents,
                                        inquiryComment.createdAt.as("createAt"))).as("reply"))
                )).stream().findFirst().orElse(null);
    }
}