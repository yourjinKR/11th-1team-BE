package org.example.knockin.repository.inquiry;

import org.example.knockin.dto.BoInquiryDetailDto;
import org.example.knockin.dto.BoInquiryListDto;
import org.example.knockin.dto.InquiryDetailDto;
import org.example.knockin.dto.InquiryListDto;
import org.example.knockin.entity.member.Member;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface InquiryRepositoryCustom {
    List<InquiryListDto.Response.InquiryItem> findMyInquiryList(Boolean isDeleted, Member member, Pageable pageable);
    InquiryDetailDto.Response.InquiryDetail findMyInquiry(Boolean isDeleted, Member member, Long inquiryId);
    List<BoInquiryListDto.Response.InquiryItem> findBackOfficeInquirieList(Pageable pageable, BoInquiryListDto.Request request);
    BoInquiryDetailDto.Response.InquiryDetail findBackOfficeInquirie(Long id);
    List<BoInquiryDetailDto.Response.InquiryDetail.Reply> findBackOfficeInquirieReply(Long id);
}