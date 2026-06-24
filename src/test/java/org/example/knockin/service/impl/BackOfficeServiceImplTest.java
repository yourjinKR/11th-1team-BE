package org.example.knockin.service.impl;

import org.example.knockin.dto.*;
import org.example.knockin.entity.agreement.Agreement;
import org.example.knockin.entity.alarm.Notification;
import org.example.knockin.entity.inquiry.Inquiry;
import org.example.knockin.entity.inquiry.InquiryComment;
import org.example.knockin.entity.life.LifePattern;
import org.example.knockin.entity.life.LifePatternInformation;
import org.example.knockin.entity.life.LifePatternType;
import org.example.knockin.entity.member.Member;
import org.example.knockin.entity.member.MemberRole;
import org.example.knockin.entity.member.MemberState;
import org.example.knockin.entity.room.RoomType;
import org.example.knockin.global.auth.exception.AuthErrorCode;
import org.example.knockin.global.exception.BusinessException;
import org.example.knockin.global.util.ReportType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.spy;

@ExtendWith(MockitoExtension.class)
@DisplayName("관리자 백오피스 서비스 테스트")
class BackOfficeServiceImplTest {

    @Mock
    private AgreementServiceImpl agreementService;

    @Mock
    private RoomTypeServiceImpl roomTypeService;

    @Mock
    private LifeStyleServiceImpl lifeStyleService;

    @Mock
    private AuthenticationServiceImpl authenticationService;

    @Mock
    private NotificationServiceImpl notificationService;

    @Mock
    private MemberServiceImpl memberService;

    @Mock
    private InquirieServiceImpl inquirieService;

    @Mock
    private DeclarationServiceImpl declarationService;

    @InjectMocks
    private BackOfficeServiceImpl backOfficeService;

    @Test
    @DisplayName("약관 등록 성공 테스트 (saveTerms)")
    void saveTermsSuccessTest() {
        // given
        BoTermsDto.Request request = new BoTermsDto.Request();
        request.setTitle("약관 제목");
        request.setContents("약관 내용");
        request.setIsRequired(true);

        // when
        BoTermsDto.Response response = backOfficeService.saveTerms(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getUpdatedAt()).isNotNull();
        verify(agreementService).saveAgreement(any(Agreement.class));
    }

    @Test
    @DisplayName("임시 약관 수정 성공 테스트 (modifyTerms)")
    void modifyTermsSuccessTest() {
        // given
        Long termsId = 100L;
        BoTermsDto.Request request = new BoTermsDto.Request();
        request.setTitle("임시 약관 제목");
        request.setContents("임시 약관 내용");
        request.setIsRequired(false);

        // when
        BoTermsDto.Response response = backOfficeService.modifyTerms(request, termsId);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getUpdatedAt()).isNotNull();
        verify(agreementService).modifyTemporaryAgreement(any(Agreement.class), eq(termsId));
    }

    @Test
    @DisplayName("약관 목록 조회 성공 테스트 (findTermsList)")
    void findTermsListSuccessTest() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        Agreement agreement = Agreement.builder()
                .id(100L)
                .title("서비스 약관")
                .build();
        ReflectionTestUtils.setField(agreement, "createdAt", LocalDateTime.now());

        given(agreementService.findAgreementList(pageable)).willReturn(List.of(agreement));

        // when
        BoTermsListDto.Response response = backOfficeService.findTermsList(pageable);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getTerms()).hasSize(1);
        BoTermsListDto.Response.TermsItem item = response.getTerms().get(0);
        assertThat(item.getId()).isEqualTo(100L);
        assertThat(item.getTitle()).isEqualTo("서비스 약관");
        assertThat(item.getCreateAt()).isNotNull();
    }

    @Test
    @DisplayName("약관 상세 조회 성공 테스트 (findTerms)")
    void findTermsSuccessTest() {
        // given
        Long termsId = 100L;
        Agreement agreement = Agreement.builder()
                .id(termsId)
                .title("상세 약관")
                .contents("상세 내용")
                .build();
        ReflectionTestUtils.setField(agreement, "createdAt", LocalDateTime.now());

        given(agreementService.findAgreement(termsId)).willReturn(agreement);

        // when
        BoTermsDetailDto.Response response = backOfficeService.findTerms(termsId);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(termsId);
        assertThat(response.getTitle()).isEqualTo("상세 약관");
        assertThat(response.getContents()).isEqualTo("상세 내용");
        assertThat(response.getCreateAt()).isNotNull();
    }

    @Test
    @DisplayName("약관 삭제 성공 테스트 (deleteTerms)")
    void deleteTermsSuccessTest() {
        // given
        Long termsId = 100L;

        // when
        BoTermsDto.Response response = backOfficeService.deleteTerms(termsId);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getUpdatedAt()).isNotNull();
        verify(agreementService).deleteAgreement(termsId);
    }

    @Test
    @DisplayName("마지막 승인 약관 수정 성공 테스트 (modifyLastTerms)")
    void modifyLastTermsSuccessTest() {
        // given
        Long termsId = 100L;
        BoTermsDto.Request request = new BoTermsDto.Request();
        request.setTitle("최종 약관");
        request.setContents("최종 내용");
        request.setIsRequired(true);

        // when
        BoTermsDto.Response response = backOfficeService.modifyLastTerms(request, termsId);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getUpdatedAt()).isNotNull();
        verify(agreementService).modifyAgreement(any(Agreement.class), eq(termsId));
    }

    @Test
    @DisplayName("방 형태 등록 성공 테스트 (saveRoomType)")
    void saveRoomTypeSuccessTest() {
        // given
        BoRoomTypeDto.Request request = new BoRoomTypeDto.Request();
        request.setName("원룸");

        // when
        BoRoomTypeDto.Response response = backOfficeService.saveRoomType(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getUpdatedAt()).isNotNull();
        verify(roomTypeService).saveRoomType(any(RoomType.class));
    }

    @Test
    @DisplayName("방 형태 목록 조회 성공 테스트 (findRoomTypeList)")
    void findRoomTypeListSuccessTest() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        RoomType roomType = RoomType.builder().id(1L).name("원룸").build();

        given(roomTypeService.findRoomTypeList(pageable)).willReturn(List.of(roomType));

        // when
        BoRoomTypeListDto.Response response = backOfficeService.findRoomTypeList(pageable);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getRoomType()).hasSize(1);
        assertThat(response.getRoomType().get(0).getId()).isEqualTo(1L);
        assertThat(response.getRoomType().get(0).getName()).isEqualTo("원룸");
    }

    @Test
    @DisplayName("방 형태 수정 성공 테스트 (modifyRoomType)")
    void modifyRoomTypeSuccessTest() {
        // given
        Long roomTypeId = 1L;
        BoRoomTypeDto.Request request = new BoRoomTypeDto.Request();
        request.setName("투룸");

        // when
        BoRoomTypeDto.Response response = backOfficeService.modifyRoomType(request, roomTypeId);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getUpdatedAt()).isNotNull();
        verify(roomTypeService).modifyRoomType(any(RoomType.class), eq(roomTypeId));
    }

    @Test
    @DisplayName("방 형태 삭제 성공 테스트 (deleteRoomType)")
    void deleteRoomTypeSuccessTest() {
        // given
        Long roomTypeId = 1L;

        // when
        BoRoomTypeDto.Response response = backOfficeService.deleteRoomType(roomTypeId);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getUpdatedAt()).isNotNull();
        verify(roomTypeService).deleteRoomType(roomTypeId);
    }

    @Test
    @DisplayName("방 형태 상세 조회 성공 테스트 (findRoomType)")
    void findRoomTypeSuccessTest() {
        // given
        Long roomTypeId = 1L;
        RoomType roomType = RoomType.builder().id(roomTypeId).name("원룸").build();

        given(roomTypeService.findRoomType(roomTypeId)).willReturn(roomType);

        // when
        BoRoomTypeDetailDto.Response response = backOfficeService.findRoomType(roomTypeId);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(roomTypeId);
        assertThat(response.getName()).isEqualTo("원룸");
    }

    @Test
    @DisplayName("라이프스타일 패턴 등록 성공 테스트 (saveLifeStylePattern)")
    void saveLifeStylePatternSuccessTest() {
        // given
        BoLifeStylePatternDto.Request.DetailItem detail = new BoLifeStylePatternDto.Request.DetailItem();
        detail.setValues("흡연");
        detail.setDescription("담배 피우는 유형");

        BoLifeStylePatternDto.Request request = new BoLifeStylePatternDto.Request();
        request.setName("흡연 여부");
        request.setType(LifePatternType.BOOLEAN);
        request.setSort(1);
        request.setDetails(List.of(detail));

        LifePattern lifePattern = LifePattern.builder().id(100L).name("흡연 여부").build();

        given(lifeStyleService.saveLifePattern(any(LifePattern.class))).willReturn(lifePattern);

        // when
        BoLifeStylePatternDto.Response response = backOfficeService.saveLifeStylePattern(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getUpdatedAt()).isNotNull();
        verify(lifeStyleService).saveLifePattern(any(LifePattern.class));
        verify(lifeStyleService).saveLifePatternInformation(anyList());
    }

    @Test
    @DisplayName("라이프스타일 패턴 목록 조회 성공 테스트 (findLifeStylePatternList)")
    void findLifeStylePatternListSuccessTest() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        BoLifeStylePatternListDto.Response expected = BoLifeStylePatternListDto.Response.builder().build();

        given(lifeStyleService.findLifeStylePatternList(pageable)).willReturn(expected);

        // when
        BoLifeStylePatternListDto.Response response = backOfficeService.findLifeStylePatternList(pageable);

        // then
        assertThat(response).isEqualTo(expected);
    }

    @Test
    @DisplayName("라이프스타일 패턴 상세 조회 성공 테스트 (findLifeStylePattern)")
    void findLifeStylePatternSuccessTest() {
        // given
        Long id = 100L;
        BoLifeStylePatternDetailDto.Response expected = BoLifeStylePatternDetailDto.Response.builder().id(id).build();

        given(lifeStyleService.findLifeStylePattern(id)).willReturn(expected);

        // when
        BoLifeStylePatternDetailDto.Response response = backOfficeService.findLifeStylePattern(id);

        // then
        assertThat(response).isEqualTo(expected);
    }

    @Test
    @DisplayName("라이프스타일 패턴 수정 성공 테스트 (modifyLifeStylePattern)")
    void modifyLifeStylePatternSuccessTest() {
        // given
        Long id = 100L;
        BoLifeStylePatternDto.Request.DetailItem detail = new BoLifeStylePatternDto.Request.DetailItem();
        detail.setValues("비흡연");
        detail.setDescription("담배 피우지 않는 유형");

        BoLifeStylePatternDto.Request request = new BoLifeStylePatternDto.Request();
        request.setName("흡연 여부 변경");
        request.setType(LifePatternType.BOOLEAN);
        request.setSort(2);
        request.setDetails(List.of(detail));

        LifePattern pattern = spy(LifePattern.builder().id(id).name("흡연 여부").dtype(LifePatternType.BOOLEAN).sort(1).build());

        given(lifeStyleService.findLifeStyle(id)).willReturn(pattern);

        // when
        BoLifeStylePatternDto.Response response = backOfficeService.modifyLifeStylePattern(request, id);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getUpdatedAt()).isNotNull();
        verify(lifeStyleService).deleteLifeInformationByPattern(pattern);
        verify(lifeStyleService).saveLifeInformation(any(LifePatternInformation.class));
        verify(pattern).modifyLifePattern("흡연 여부 변경", LifePatternType.BOOLEAN, 2);
    }

    @Test
    @DisplayName("라이프스타일 패턴 삭제 성공 테스트 (deleteLifeStylePattern)")
    void deleteLifeStylePatternSuccessTest() {
        // given
        Long id = 100L;

        // when
        BoLifeStylePatternDto.Response response = backOfficeService.deleteLifeStylePattern(id);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getUpdatedAt()).isNotNull();
        verify(lifeStyleService).deleteLifePattern(id);
    }

    @Test
    @DisplayName("승인된 신원 인증 목록 조회 성공 테스트 (findVerificationApproves)")
    void findVerificationApprovesTest() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        BoVerificationApproveListDto.Response.EmployeeAuthItem item = new BoVerificationApproveListDto.Response.EmployeeAuthItem();
        given(authenticationService.findVerificationApproves(pageable)).willReturn(List.of(item));

        // when
        BoVerificationApproveListDto.Response response = backOfficeService.findVerificationApproves(pageable);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getEmployeeAuth()).hasSize(1);
    }

    @Test
    @DisplayName("반려된 신원 인증 목록 조회 성공 테스트 (findVerificationCancels)")
    void findVerificationCancelsTest() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        BoVerificationCancelListDto.Response.EmployeeAuthItem item = new BoVerificationCancelListDto.Response.EmployeeAuthItem();
        given(authenticationService.findVerificationCancels(pageable)).willReturn(List.of(item));

        // when
        BoVerificationCancelListDto.Response response = backOfficeService.findVerificationCancels(pageable);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getEmployeeAuth()).hasSize(1);
    }

    @Test
    @DisplayName("대기 중인 신원 인증 목록 조회 성공 테스트 (findVerificationsList)")
    void findVerificationsListTest() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        BoVerificationWaitingListDto.Response.EmployeeAuthItem item = new BoVerificationWaitingListDto.Response.EmployeeAuthItem();
        given(authenticationService.findVerificationsList(pageable)).willReturn(List.of(item));

        // when
        BoVerificationWaitingListDto.Response response = backOfficeService.findVerificationsList(pageable);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getEmployeeAuth()).hasSize(1);
    }

    @Test
    @DisplayName("인증 대기 상세 조회 성공 테스트 (findVerifications)")
    void findVerificationsDetailTest() {
        // given
        Long id = 1L;
        BoVerificationWaitingDetailDto.Response detail = new BoVerificationWaitingDetailDto.Response();
        given(authenticationService.findVerifications(id)).willReturn(detail);

        // when
        BoVerificationWaitingDetailDto.Response response = backOfficeService.findVerifications(id);

        // then
        assertThat(response).isEqualTo(detail);
    }

    @Test
    @DisplayName("신원 인증 승인 처리 성공 테스트 (saveVerifications)")
    void saveVerificationsSuccessTest() {
        // given
        Long id = 1L;

        // when
        BoVerificationDto.Response response = backOfficeService.saveVerifications(id);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getUpdatedAt()).isNotNull();
        verify(authenticationService).saveVerifications(id);
    }

    @Test
    @DisplayName("신원 인증 반려 처리 성공 테스트 (deleteVerifications)")
    void deleteVerificationsSuccessTest() {
        // given
        Long id = 1L;

        // when
        BoVerificationDto.Response response = backOfficeService.deleteVerifications(id);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getUpdatedAt()).isNotNull();
        verify(authenticationService).deleteVerifications(id);
    }

    @Test
    @DisplayName("공지사항 등록 성공 테스트 (saveNotice)")
    void saveNoticeSuccessTest() {
        // given
        Long memberId = 1L;
        BoNoticeDto.Request request = new BoNoticeDto.Request("공지 제목", "공지 내용");
        Member member = mock(Member.class);

        given(memberService.findById(memberId)).willReturn(Optional.of(member));

        // when
        BoNoticeDto.Response response = backOfficeService.saveNotice(request, memberId);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getUpdatedAt()).isNotNull();
        verify(notificationService).saveNotification(any(Notification.class));
    }

    @Test
    @DisplayName("공지사항 등록 시 회원을 찾을 수 없으면 BusinessException 발생")
    void saveNoticeMemberNotFoundTest() {
        // given
        Long memberId = 1L;
        BoNoticeDto.Request request = new BoNoticeDto.Request("공지 제목", "공지 내용");

        given(memberService.findById(memberId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> backOfficeService.saveNotice(request, memberId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", AuthErrorCode.MEMBER_NOT_FOUND);
    }

    @Test
    @DisplayName("공지사항 목록 조회 성공 테스트 (findNoticeList)")
    void findNoticeListSuccessTest() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        BoNoticeListDto.Response.NoticeItem item = BoNoticeListDto.Response.NoticeItem.builder()
                .id(1L)
                .title("공지 제목")
                .writer("작성자")
                .build();

        given(notificationService.findNotificationList(pageable)).willReturn(List.of(item));

        // when
        BoNoticeListDto.Response response = backOfficeService.findNoticeList(pageable);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getNotices()).hasSize(1);
        assertThat(response.getNotices().get(0).getId()).isEqualTo(1L);
        assertThat(response.getNotices().get(0).getTitle()).isEqualTo("공지 제목");
    }

    @Test
    @DisplayName("공지사항 상세 조회 성공 테스트 (findNotice)")
    void findNoticeSuccessTest() {
        // given
        Long id = 1L;
        BoNoticeDetailDto.Response expected = new BoNoticeDetailDto.Response();
        BoNoticeDetailDto.Response.NoticeDetail noticeDetail = new BoNoticeDetailDto.Response.NoticeDetail();
        noticeDetail.setId(id);
        noticeDetail.setTitle("공지 제목");
        expected.setNotice(noticeDetail);

        given(notificationService.findNotification(id)).willReturn(expected);

        // when
        BoNoticeDetailDto.Response response = backOfficeService.findNotice(id);

        // then
        assertThat(response).isEqualTo(expected);
    }

    @Test
    @DisplayName("공지사항 수정 성공 테스트 (modifyNotice)")
    void modifyNoticeSuccessTest() {
        // given
        Long id = 1L;
        Long memberId = 2L;
        BoNoticeDto.Request request = new BoNoticeDto.Request("수정 제목", "수정 내용");
        Member member = mock(Member.class);
        Notification notification = spy(Notification.builder()
                .id(id)
                .title("기존 제목")
                .contents("기존 내용")
                .build());

        given(memberService.findById(memberId)).willReturn(Optional.of(member));
        given(notificationService.findNotificationById(id)).willReturn(notification);

        // when
        BoNoticeDto.Response response = backOfficeService.modifyNotice(request, id, memberId);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getUpdatedAt()).isNotNull();
        verify(notification).modifyNotification(request, member);
    }

    @Test
    @DisplayName("공지사항 수정 시 회원을 찾을 수 없으면 BusinessException 발생")
    void modifyNoticeMemberNotFoundTest() {
        // given
        Long id = 1L;
        Long memberId = 2L;
        BoNoticeDto.Request request = new BoNoticeDto.Request("수정 제목", "수정 내용");

        given(memberService.findById(memberId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> backOfficeService.modifyNotice(request, id, memberId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", AuthErrorCode.MEMBER_NOT_FOUND);
    }

    @Test
    @DisplayName("공지사항 삭제 성공 테스트 (deleteNotice)")
    void deleteNoticeSuccessTest() {
        // given
        Long id = 1L;
        Long memberId = 2L;
        Member member = mock(Member.class);
        Notification notification = spy(Notification.builder()
                .id(id)
                .isDeleted(false)
                .build());

        given(memberService.findById(memberId)).willReturn(Optional.of(member));
        given(notificationService.findNotificationById(id)).willReturn(notification);

        // when
        BoNoticeDto.Response response = backOfficeService.deleteNotice(id, memberId);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getUpdatedAt()).isNotNull();
        verify(notification).deleteNotification(member);
    }

    @Test
    @DisplayName("공지사항 삭제 시 회원을 찾을 수 없으면 BusinessException 발생")
    void deleteNoticeMemberNotFoundTest() {
        // given
        Long id = 1L;
        Long memberId = 2L;

        given(memberService.findById(memberId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> backOfficeService.deleteNotice(id, memberId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", AuthErrorCode.MEMBER_NOT_FOUND);
    }

    @Test
    @DisplayName("문의 답변 등록 성공 테스트 (saveInquiryReply)")
    void saveInquiryReplySuccessTest() {
        // given
        Long memberId = 1L;
        BoInquiryReplyDto.Request request = new BoInquiryReplyDto.Request();
        request.setInquirieId(100L);
        request.setContents("답변 내용");

        Member member = mock(Member.class);
        Inquiry inquiry = mock(Inquiry.class);

        given(memberService.findById(memberId)).willReturn(Optional.of(member));
        given(inquirieService.findInquiryById(100L)).willReturn(inquiry);

        // when
        BoInquiryReplyDto.Response response = backOfficeService.saveInquiryReply(request, memberId);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getUpdatedAt()).isNotNull();
        verify(inquirieService).saveInquirieReply(any(InquiryComment.class));
    }

    @Test
    @DisplayName("문의 답변 등록 시 회원을 찾을 수 없으면 BusinessException 발생")
    void saveInquiryReplyMemberNotFoundTest() {
        // given
        Long memberId = 1L;
        BoInquiryReplyDto.Request request = new BoInquiryReplyDto.Request();
        request.setInquirieId(100L);

        given(memberService.findById(memberId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> backOfficeService.saveInquiryReply(request, memberId));
    }

    @DisplayName("회원 목록 조회 성공 테스트 (findMemberList)")
    void findMemberListSuccessTest() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        BoMemberListDto.Response expected = BoMemberListDto.Response.builder().build();

        given(memberService.findBackOfficeMemberList(pageable)).willReturn(expected);

        // when
        BoMemberListDto.Response response = backOfficeService.findMemberList(pageable);

        // then
        assertThat(response).isEqualTo(expected);
        verify(memberService).findBackOfficeMemberList(pageable);
    }

    @Test
    @DisplayName("회원 상세 조회 성공 테스트 (findMember)")
    void findMemberSuccessTest() {
        // given
        Long id = 1L;
        Member member = mock(Member.class);
        BoMemberDetailDto.Response expected = new BoMemberDetailDto.Response();

        given(memberService.findById(id)).willReturn(Optional.of(member));
        given(memberService.findBackOfficeMember(id)).willReturn(expected);

        // when
        BoMemberDetailDto.Response response = backOfficeService.findMember(id);

        // then
        assertThat(response).isEqualTo(expected);
        verify(memberService).findBackOfficeMember(id);
    }

    @Test
    @DisplayName("회원 상세 조회 시 회원을 찾지 못하면 BusinessException 발생")
    void findMemberNotFoundTest() {
        // given
        Long id = 1L;
        given(memberService.findById(id)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> backOfficeService.findMember(id))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", AuthErrorCode.MEMBER_NOT_FOUND);
    }

    @Test
    @DisplayName("문의 목록 조회 성공 테스트 (findInquirieList)")
    void findInquirieListSuccessTest() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        BoInquiryListDto.Response.InquiryItem item = new BoInquiryListDto.Response.InquiryItem();
        item.setId(100L);
        item.setTitle("문의 제목");

        given(inquirieService.findBackOfficeInquirieList(pageable)).willReturn(List.of(item));

        // when
        BoInquiryListDto.Response response = backOfficeService.findInquirieList(pageable);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getInquiries()).hasSize(1);
        assertThat(response.getInquiries().get(0).getId()).isEqualTo(100L);
        assertThat(response.getInquiries().get(0).getTitle()).isEqualTo("문의 제목");
    }

    @Test
    @DisplayName("문의 상세 조회 성공 테스트 (findInquirie)")
    void findInquirieSuccessTest() {
        // given
        Long inquiryId = 100L;
        BoInquiryDetailDto.Response.InquiryDetail detail = new BoInquiryDetailDto.Response.InquiryDetail();
        detail.setId(inquiryId);
        detail.setTitle("문의 제목");

        given(inquirieService.findBackOfficeInquirie(inquiryId)).willReturn(detail);

        // when
        BoInquiryDetailDto.Response response = backOfficeService.findInquirie(inquiryId);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getInquirie()).isEqualTo(detail);
    }

    @DisplayName("회원 삭제/비활성화 성공 테스트 (deleteMember)")
    void deleteMemberSuccessTest() {
        // given
        Long id = 1L;
        Member member = mock(Member.class);

        given(memberService.findById(id)).willReturn(Optional.of(member));

        // when
        BoMemberCancelDto.Response response = backOfficeService.deleteMember(id);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getUpdatedAt()).isNotNull();
        verify(memberService).setMemberState(member, MemberState.INACTIVE);
    }

    @Test
    @DisplayName("회원 삭제/비활성화 시 회원을 찾지 못하면 BusinessException 발생")
    void deleteMemberNotFoundTest() {
        // given
        Long id = 1L;
        given(memberService.findById(id)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> backOfficeService.deleteMember(id))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", AuthErrorCode.MEMBER_NOT_FOUND);
    }

    @Test
    @DisplayName("회원 권한 부여 성공 테스트 (authMember)")
    void authMemberSuccessTest() {
        // given
        Long id = 1L;
        BoMemberAuthDto.Request request = new BoMemberAuthDto.Request(MemberRole.ADMIN);
        Member member = mock(Member.class);

        given(memberService.findById(id)).willReturn(Optional.of(member));

        // when
        BoMemberAuthDto.Response response = backOfficeService.authMember(id, request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getUpdatedAt()).isNotNull();
        verify(memberService).setMemberAuth(member, MemberRole.ADMIN);
    }

    @Test
    @DisplayName("회원 권한 부여 시 회원을 찾을 수 없으면 BusinessException 발생")
    void authMemberNotFoundTest() {
        // given
        Long id = 1L;
        BoMemberAuthDto.Request request = new BoMemberAuthDto.Request(MemberRole.ADMIN);

        given(memberService.findById(id)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> backOfficeService.authMember(id, request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", AuthErrorCode.MEMBER_NOT_FOUND);
    }

    @Test
    @DisplayName("신고 대기 목록 조회 성공 테스트 (findReportWaitList)")
    void findReportWaitListSuccessTest() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        BoReportWaitListDto.Response.ReportInfo info = new BoReportWaitListDto.Response.ReportInfo();
        info.setId(100L);

        given(declarationService.findReportWaitList(pageable)).willReturn(List.of(info));

        // when
        BoReportWaitListDto.Response response = backOfficeService.findReportWaitList(pageable);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getReportInfoList()).hasSize(1);
        assertThat(response.getReportInfoList().get(0).getId()).isEqualTo(100L);
        verify(declarationService).findReportWaitList(pageable);
    }

    @Test
    @DisplayName("신고 완료 목록 조회 성공 테스트 (findReportDoneList)")
    void findReportDoneListSuccessTest() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        BoReportDoneListDto.Response.ReportInfo info = new BoReportDoneListDto.Response.ReportInfo();
        info.setId(100L);

        given(declarationService.findReportDoneList(pageable)).willReturn(List.of(info));

        // when
        BoReportDoneListDto.Response response = backOfficeService.findReportDoneList(pageable);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getReportInfoList()).hasSize(1);
        assertThat(response.getReportInfoList().get(0).getId()).isEqualTo(100L);
        verify(declarationService).findReportDoneList(pageable);
    }

    @Test
    @DisplayName("신고 숨김 처리 성공 테스트 (reportHidden)")
    void reportHiddenSuccessTest() {
        // given
        BoReportHiddenDto.Request request = new BoReportHiddenDto.Request();
        request.setId(100L);
        request.setType(ReportType.BOARD);
        request.setReason("사유");

        // when
        BoReportHiddenDto.Response response = backOfficeService.reportHidden(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getUpdatedAt()).isNotNull();
        verify(declarationService).reportHidden(100L, ReportType.BOARD, "사유");
    }

    @Test
    @DisplayName("신고 무조치 처리 성공 테스트 (reportNoAction)")
    void reportNoActionSuccessTest() {
        // given
        BoReportNoActionDto.Request request = new BoReportNoActionDto.Request();
        request.setId(100L);
        request.setType(ReportType.MEMBER);

        // when
        BoReportNoActionDto.Response response = backOfficeService.reportNoAction(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getUpdatedAt()).isNotNull();
        verify(declarationService).reportNoAction(100L, ReportType.MEMBER);
    }

    @Test
    @DisplayName("신고 정지 처리 성공 테스트 (reportSuspended)")
    void reportSuspendedSuccessTest() {
        // given
        BoReportSuspendedDto.Request request = new BoReportSuspendedDto.Request();
        request.setId(100L);
        request.setType(ReportType.BOARD);
        request.setReason("사유");

        // when
        BoReportSuspendedDto.Response response = backOfficeService.reportSuspended(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getUpdatedAt()).isNotNull();
        verify(declarationService).reportSuspended(100L, ReportType.BOARD, "사유");
    }
}
