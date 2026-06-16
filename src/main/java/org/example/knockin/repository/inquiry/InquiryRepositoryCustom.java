package org.example.knockin.repository.inquiry;

import org.example.knockin.dto.InquiryDetailDto;
import org.example.knockin.dto.InquiryListDto;
import org.example.knockin.entity.member.Member;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface InquiryRepositoryCustom {
    List<InquiryListDto.Response.InquiryItem> findMyInquiryList(Boolean isDeleted, Member member, Pageable pageable);
    InquiryDetailDto.Response.InquiryDetail findMyInquiry(Boolean isDeleted, Member member, Long inquiryId);
}