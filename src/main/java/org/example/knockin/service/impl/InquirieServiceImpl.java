package org.example.knockin.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.knockin.dto.*;
import org.example.knockin.entity.inquiry.Inquiry;
import org.example.knockin.entity.inquiry.InquiryCategory;
import org.example.knockin.entity.inquiry.InquiryComment;
import org.example.knockin.entity.member.Member;
import org.example.knockin.global.auth.exception.AuthErrorCode;
import org.example.knockin.global.exception.BusinessException;
import org.example.knockin.global.exception.InquiryErrorCode;
import org.example.knockin.repository.inquiry.InquiryCategoryRepository;
import org.example.knockin.repository.inquiry.InquiryCommentRepository;
import org.example.knockin.repository.inquiry.InquiryRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InquirieServiceImpl {
    private final MemberServiceImpl memberService;
    private final InquiryCategoryRepository inquiryCategoryRepository;
    private final InquiryRepository inquiryRepository;
    private final InquiryCommentRepository inquiryCommentRepository;

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

    public InquiryCategoryListDto.Response findInquirieCategoryList() {
        List<InquiryCategoryListDto.Response.Category> categoryList = inquiryCategoryRepository.findAllByIsDeleted(false).stream().map(item -> InquiryCategoryListDto.Response.Category.builder().id(item.getId()).name(item.getTitle()).build()).toList();
        return InquiryCategoryListDto.Response.builder().inquirieCategorys(categoryList).build();
    }

    public InquiryListDto.Response findInquirieList(Pageable pageable, Long memberId) {
        Member member = memberService.findById(memberId).orElseThrow(() -> new BusinessException(AuthErrorCode.MEMBER_NOT_FOUND));
        List<InquiryListDto.Response.InquiryItem> inquiryItemList = inquiryRepository.findMyInquiryList(false, member, pageable);
        return InquiryListDto.Response.builder().inquiries(inquiryItemList).build();
    }

    public InquiryDetailDto.Response findInquirie(Long inquiryId, Long memberId) {
        Member member = memberService.findById(memberId).orElseThrow(() -> new BusinessException(AuthErrorCode.MEMBER_NOT_FOUND));
        return InquiryDetailDto.Response.builder().inquirie(inquiryRepository.findMyInquiry(false, member, inquiryId)).build();
    }

    @Transactional
    public InquiryComment saveInquirieReply(InquiryComment inquiryComment) {
        return inquiryCommentRepository.save(inquiryComment);
    }

    public Inquiry findInquiryById(Long inquiryId) {
        return inquiryRepository.findByIdAndIsDeleted(inquiryId, false).orElseThrow(() -> new BusinessException(InquiryErrorCode.INQUIRY_NOT_FOUND));
    }

    public List<BoInquiryListDto.Response.InquiryItem> findBackOfficeInquirieList(Pageable pageable, BoInquiryListDto.Request request) {
        return inquiryRepository.findBackOfficeInquirieList(pageable, request);
    }

    public BoInquiryDetailDto.Response.InquiryDetail findBackOfficeInquirie(Long id) {
        BoInquiryDetailDto.Response.InquiryDetail inquiryDetail = inquiryRepository.findBackOfficeInquirie(id);
        inquiryDetail.setReply(inquiryRepository.findBackOfficeInquirieReply(id));
        return inquiryDetail;
    }
}
