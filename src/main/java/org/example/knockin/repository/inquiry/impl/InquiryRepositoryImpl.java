package org.example.knockin.repository.inquiry.impl;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import org.example.knockin.dto.InquiryListDto;
import org.example.knockin.entity.member.Member;
import org.example.knockin.repository.inquiry.InquiryRepositoryCustom;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

import static org.example.knockin.entity.inquiry.QInquiry.inquiry;
import static org.example.knockin.entity.member.QMember.member;
import static org.example.knockin.entity.member.QBasicInformation.basicInformation;
import static org.example.knockin.entity.inquiry.QInquiryComment.inquiryComment;

@Repository
@RequiredArgsConstructor
public class InquiryRepositoryImpl implements InquiryRepositoryCustom {
    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<InquiryListDto.Response.InquiryItem> findMyInquiry(Boolean isDeleted, Member memberEntity, Pageable pageable) {
        return jpaQueryFactory.select(Projections.fields(InquiryListDto.Response.InquiryItem.class,
                inquiry.id,
                inquiry.title,
                inquiry.createdAt,
                inquiry.inquiryCategory.title.as("type"),
                        basicInformation.name.as("writer"),
                new CaseBuilder().when(inquiryComment.id.isNull()).then("답변 대기중").otherwise("답변 완료").as("status")
                )).from(inquiry).leftJoin(inquiry.inquiryCategory)
                .leftJoin(inquiryComment).on(inquiryComment.inquiry.eq(inquiry))
                .leftJoin(basicInformation).on(basicInformation.member.eq(inquiry.member))
                .where(inquiry.isDeleted.eq(isDeleted), inquiry.member.eq(memberEntity))
                .offset(pageable.getOffset()).limit(pageable.getPageSize()).fetch();
    }
}