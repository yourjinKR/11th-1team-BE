package org.example.knockin.service.impl;

import org.example.knockin.dto.InquiryCategoryListDto;
import org.example.knockin.dto.InquiryDetailDto;
import org.example.knockin.dto.InquiryDto;
import org.example.knockin.dto.InquiryListDto;
import org.example.knockin.entity.inquiry.Inquiry;
import org.example.knockin.entity.inquiry.InquiryCategory;
import org.example.knockin.entity.member.Member;
import org.example.knockin.global.auth.exception.AuthErrorCode;
import org.example.knockin.global.exception.BusinessException;
import org.example.knockin.global.exception.InquiryErrorCode;
import org.example.knockin.repository.inquiry.InquiryCategoryRepository;
import org.example.knockin.repository.inquiry.InquiryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("1:1 문의 서비스 테스트")
class InquirieServiceImplTest {

    @Mock
    private MemberServiceImpl memberService;

    @Mock
    private InquiryCategoryRepository inquiryCategoryRepository;

    @Mock
    private InquiryRepository inquiryRepository;

    @InjectMocks
    private InquirieServiceImpl inquiriesService;

    @Test
    @DisplayName("문의 저장 성공 테스트 (saveInquiry)")
    void saveInquirySuccessTest() {
        // given
        InquiryDto.Request request = new InquiryDto.Request();
        request.setCategoryId(1L);
        request.setTitle("Inquiry Title");
        request.setContents("Inquiry Contents");

        Member member = mock(Member.class);
        InquiryCategory category = mock(InquiryCategory.class);

        given(inquiryCategoryRepository.findById(1L)).willReturn(Optional.of(category));

        // when
        inquiriesService.saveInquiry(request, member);

        // then
        verify(inquiryRepository).save(any(Inquiry.class));
    }

    @Test
    @DisplayName("문의 저장 시 카테고리가 없으면 BusinessException 발생")
    void saveInquiryCategoryNotFoundTest() {
        // given
        InquiryDto.Request request = new InquiryDto.Request();
        request.setCategoryId(1L);

        Member member = mock(Member.class);

        given(inquiryCategoryRepository.findById(1L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> inquiriesService.saveInquiry(request, member))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", InquiryErrorCode.INQUIRY_CATEGORY_NOT_FOUND);

        verifyNoInteractions(inquiryRepository);
    }

    @Test
    @DisplayName("문의 저장 비즈니스 로직 성공 테스트 (saveInquiryLogic)")
    void saveInquiryLogicSuccessTest() {
        // given
        Long memberId = 1L;
        InquiryDto.Request request = new InquiryDto.Request();
        request.setCategoryId(1L);
        request.setTitle("Inquiry Title");
        request.setContents("Inquiry Contents");

        Member member = mock(Member.class);
        InquiryCategory category = mock(InquiryCategory.class);

        given(memberService.findById(memberId)).willReturn(Optional.of(member));
        given(inquiryCategoryRepository.findById(1L)).willReturn(Optional.of(category));

        // when
        InquiryDto.Response response = inquiriesService.saveInquiryLogic(request, memberId);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getUpdatedAt()).isNotNull();
        verify(inquiryRepository).save(any(Inquiry.class));
    }

    @Test
    @DisplayName("문의 저장 로직 실행 시 회원이 없으면 BusinessException 발생")
    void saveInquiryLogicMemberNotFoundTest() {
        // given
        Long memberId = 1L;
        InquiryDto.Request request = new InquiryDto.Request();
        given(memberService.findById(memberId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> inquiriesService.saveInquiryLogic(request, memberId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", AuthErrorCode.MEMBER_NOT_FOUND);

        verifyNoInteractions(inquiryCategoryRepository);
        verifyNoInteractions(inquiryRepository);
    }

    @Test
    @DisplayName("삭제되지 않은 문의 카테고리 목록 조회 성공 테스트")
    void findInquiryCategoryListSuccessTest() {
        // given
        InquiryCategory category1 = InquiryCategory.builder().id(1L).title("General").isDeleted(false).build();
        InquiryCategory category2 = InquiryCategory.builder().id(2L).title("Payment").isDeleted(false).build();

        given(inquiryCategoryRepository.findAllByIsDeleted(false)).willReturn(List.of(category1, category2));

        // when
        InquiryCategoryListDto.Response response = inquiriesService.findInquirieCategoryList();

        // then
        assertThat(response).isNotNull();
        assertThat(response.getInquirieCategorys()).hasSize(2);
        assertThat(response.getInquirieCategorys().get(0).getName()).isEqualTo("General");
        assertThat(response.getInquirieCategorys().get(1).getName()).isEqualTo("Payment");
    }

    @Test
    @DisplayName("회원의 문의 목록 조회 성공 테스트")
    void findInquiryListSuccessTest() {
        // given
        Long memberId = 1L;
        Member member = mock(Member.class);
        Pageable pageable = PageRequest.of(0, 10);
        InquiryListDto.Response.InquiryItem item = InquiryListDto.Response.InquiryItem.builder()
                .id(100L)
                .title("Inquiry Title")
                .build();

        given(memberService.findById(memberId)).willReturn(Optional.of(member));
        given(inquiryRepository.findMyInquiryList(false, member, pageable)).willReturn(List.of(item));

        // when
        InquiryListDto.Response response = inquiriesService.findInquirieList(pageable, memberId);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getInquiries()).hasSize(1);
        assertThat(response.getInquiries().get(0).getId()).isEqualTo(100L);
    }

    @Test
    @DisplayName("회원의 문의 상세 조회 성공 테스트")
    void findInquiryDetailSuccessTest() {
        // given
        Long memberId = 1L;
        Long inquiryId = 100L;
        Member member = mock(Member.class);
        InquiryDetailDto.Response.InquiryDetail detail = InquiryDetailDto.Response.InquiryDetail.builder()
                .id(inquiryId)
                .title("Title")
                .contents("Contents")
                .build();

        given(memberService.findById(memberId)).willReturn(Optional.of(member));
        given(inquiryRepository.findMyInquiry(false, member, inquiryId)).willReturn(detail);

        // when
        InquiryDetailDto.Response response = inquiriesService.findInquirie(inquiryId, memberId);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getInquirie()).isEqualTo(detail);
    }
}
