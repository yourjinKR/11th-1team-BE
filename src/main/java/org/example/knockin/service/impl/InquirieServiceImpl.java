package org.example.knockin.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.knockin.dto.InquiryDto;
import org.example.knockin.entity.inquiry.Inquiry;
import org.example.knockin.entity.inquiry.InquiryCategory;
import org.example.knockin.entity.member.Member;
import org.example.knockin.global.auth.exception.AuthErrorCode;
import org.example.knockin.global.exception.BusinessException;
import org.example.knockin.global.exception.InquiryErrorCode;
import org.example.knockin.repository.inquiry.InquiryCategoryRepository;
import org.example.knockin.repository.inquiry.InquiryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class InquirieServiceImpl {
    private final MemberServiceImpl memberService;
    private final InquiryCategoryRepository inquiryCategoryRepository;
    private final InquiryRepository inquiryRepository;

    @Transactional
    public void saveInquiry(InquiryDto.Request request, Member member) {
        InquiryCategory inquiryCategory = inquiryCategoryRepository.findById(request.getCategoryId()).orElseThrow(() -> new BusinessException(InquiryErrorCode.INQUIRY_CATEGORY_NOT_FOUND));
        inquiryRepository.save(Inquiry.builder().inquiryCategory(inquiryCategory).member(member).title(request.getTitle()).contents(request.getContents()).build());
    }

    @Transactional
    public InquiryDto.Response saveInquiryLogic(InquiryDto.Request request, Long memberId) {
        Member member = memberService.findById(memberId).orElseThrow(() -> new BusinessException(AuthErrorCode.MEMBER_NOT_FOUND));
        saveInquiry(request, member);
        return InquiryDto.Response.builder().updatedAt(LocalDateTime.now()).build();
    }
}
