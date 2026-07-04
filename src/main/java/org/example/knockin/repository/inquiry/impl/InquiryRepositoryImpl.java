package org.example.knockin.repository.inquiry.impl;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.example.knockin.dto.BoInquiryDetailDto;
import org.example.knockin.dto.BoInquiryListDto;
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

    @Override
    public List<BoInquiryListDto.Response.InquiryItem> findBackOfficeInquirieList(Pageable pageable, BoInquiryListDto.Request request) {
        return jpaQueryFactory.select(Projections.fields(BoInquiryListDto.Response.InquiryItem.class,
                inquiry.id,
                inquiry.title,
                inquiry.createdAt,
                inquiry.inquiryCategory.title.as("type"), basicInformation.name.as("writer"),
                new CaseBuilder().when(inquiryComment.id.isNull()).then("답변 대기중").otherwise("답변 완료").as("status")
                )).from(inquiry).leftJoin(inquiry.inquiryCategory)
                .leftJoin(inquiryComment).on(inquiryComment.inquiry.eq(inquiry))
                .leftJoin(basicInformation).on(basicInformation.member.eq(inquiry.member))
                .where(inquiry.isDeleted.eq(false), searchTitle(request.getSearchKeyword()).or(searchMemberName(request.getSearchKeyword()).or(searchState(request.getIsReply()).or(searchType(request.getCategoryId())))))
                .offset(pageable.getOffset()).limit(pageable.getPageSize()).fetch();
    }

    @Override
    public BoInquiryDetailDto.Response.InquiryDetail findBackOfficeInquirie(Long id) {
        return jpaQueryFactory.select(Projections.fields(BoInquiryDetailDto.Response.InquiryDetail.class,
                        inquiry.id,
                        inquiry.title,
                        inquiry.contents,
                        inquiry.createdAt,
                        inquiry.inquiryCategory.title.as("type"), basicInformation.name.as("writer"),
                        new CaseBuilder().when(inquiryComment.id.isNull()).then("답변 대기중").otherwise("답변 완료").as("status")
                )).from(inquiry).leftJoin(inquiry.inquiryCategory)
                .leftJoin(inquiryComment).on(inquiryComment.inquiry.eq(inquiry))
                .leftJoin(basicInformation).on(basicInformation.member.eq(inquiry.member))
                .where(inquiry.isDeleted.eq(false)).fetchOne();
    }

    @Override
    public List<BoInquiryDetailDto.Response.InquiryDetail.Reply> findBackOfficeInquirieReply(Long id) {
        return jpaQueryFactory.select(Projections.fields(
                        BoInquiryDetailDto.Response.InquiryDetail.Reply.class,
                        inquiryComment.id,
                        inquiryComment.contents,
                        basicInformation.name.as("writer"),
                        inquiryComment.createdAt.as("createAt")
                ))
                .from(inquiryComment)
                .join(inquiryComment.inquiry, inquiry)
                .join(basicInformation).on(basicInformation.member.eq(inquiryComment.member))
                .where(inquiry.id.eq(id))
                .fetch();
    }

    private BooleanExpression searchMemberName(String name) {
        return name != null ? basicInformation.name.contains(name) : null;
    }

    private BooleanExpression searchTitle(String title) {
        return title != null ? inquiry.title.contains(title) : null;
    }

    private BooleanExpression searchState(Boolean isReplied) {
        if (isReplied == null) return null;
        return isReplied ? inquiryComment.id.isNotNull() : inquiryComment.id.isNull();
    }

    private BooleanExpression searchType(Long typeId) {
        return typeId != null ? inquiry.inquiryCategory.id.eq(typeId) : null;
    }
}