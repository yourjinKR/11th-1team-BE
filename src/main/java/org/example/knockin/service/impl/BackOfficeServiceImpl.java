package org.example.knockin.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.knockin.dto.*;
import org.example.knockin.entity.agreement.Agreement;
import org.example.knockin.entity.agreement.AgreementType;
import org.example.knockin.entity.alarm.Notification;
import org.example.knockin.entity.inquiry.Inquiry;
import org.example.knockin.entity.inquiry.InquiryComment;
import org.example.knockin.entity.life.LifePattern;
import org.example.knockin.entity.life.LifePatternInformation;
import org.example.knockin.entity.member.Member;
import org.example.knockin.entity.member.MemberState;
import org.example.knockin.entity.room.RoomType;
import org.example.knockin.exception.AuthErrorCode;
import org.example.knockin.exception.BusinessException;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BackOfficeServiceImpl {
    private final AgreementServiceImpl agreementService;
    private final RoomTypeServiceImpl roomTypeService;
    private final LifeStyleServiceImpl lifeStyleService;
    private final AuthenticationServiceImpl authenticationService;
    private final NotificationServiceImpl notificationService;
    private final MemberServiceImpl memberService;
    private final InquirieServiceImpl inquirieService;
    private final DeclarationServiceImpl declarationService;
    private final RoommateBoardServiceImpl roommateBoardService;

    @Transactional
    public BoTermsDto.Response saveTerms(BoTermsDto.Request request) {
        AgreementType agreementType = agreementService.findAgreementTypeById(request.getAgreementTypeId());
        agreementService.saveAgreement(Agreement.builder().title(request.getTitle()).contents(request.getContents()).isRequired(request.getIsRequired()).type(agreementType).build());
        return BoTermsDto.Response.builder().updatedAt(LocalDateTime.now()).build();
    }

    @Transactional
    public BoTermsDto.Response modifyTerms(BoTermsDto.Request request, Long termsId) {
        AgreementType type = agreementService.findAgreementType(termsId);
        agreementService.modifyTemporaryAgreement(Agreement.builder().title(request.getTitle()).contents(request.getContents()).isRequired(request.getIsRequired()).type(type).isDeleted(false).build());
        return BoTermsDto.Response.builder().updatedAt(LocalDateTime.now()).build();
    }

    public BoTermsListDto.Response findTermsList(Pageable pageable, BoTermsListDto.Request request) {
        List<BoTermsListDto.Response.TermsItem> termsItemList = agreementService.findAgreementList(pageable, request.getAgreementTypeId());
        return BoTermsListDto.Response.builder().terms(termsItemList).build();
    }

    public BoTermsDetailDto.Response findTerms(Long termsId) {
        Agreement agreement = agreementService.findAgreement(termsId);
        return BoTermsDetailDto.Response.builder().id(agreement.getId()).title(agreement.getTitle()).contents(agreement.getContents()).createAt(agreement.getCreatedAt()).build();
    }

    @Transactional
    public BoTermsDto.Response deleteTerms(Long termsId) {
        agreementService.deleteAgreement(termsId);
        return BoTermsDto.Response.builder().updatedAt(LocalDateTime.now()).build();
    }

    @Transactional
    public BoTermsDto.Response modifyLastTerms(BoTermsDto.Request request, Long termsId) {
        agreementService.modifyAgreement(Agreement.builder().title(request.getTitle()).contents(request.getContents()).isRequired(request.getIsRequired()).build(), termsId);
        return BoTermsDto.Response.builder().updatedAt(LocalDateTime.now()).build();
    }

    @Transactional
    public BoRoomTypeDto.Response saveRoomType(BoRoomTypeDto.Request request) {
        roomTypeService.saveRoomType(RoomType.builder().name(request.getName()).build());
        return BoRoomTypeDto.Response.builder().updatedAt(LocalDateTime.now()).build();
    }

    public BoRoomTypeListDto.Response findRoomTypeList(Pageable pageable) {
        List<BoRoomTypeListDto.Response.RoomTypeItem> roomTypeItemList = roomTypeService.findRoomTypeList(pageable).stream().map(item ->
                BoRoomTypeListDto.Response.RoomTypeItem.builder().id(item.getId()).name(item.getName()).build()).toList();
        return BoRoomTypeListDto.Response.builder().roomType(roomTypeItemList).build();
    }

    @Transactional
    public BoRoomTypeDto.Response modifyRoomType(BoRoomTypeDto.Request request, Long roomTypeId) {
        roomTypeService.modifyRoomType(RoomType.builder().name(request.getName()).build(), roomTypeId);
        return BoRoomTypeDto.Response.builder().updatedAt(LocalDateTime.now()).build();
    }

    @Transactional
    public BoRoomTypeDto.Response deleteRoomType(Long roomTypeId) {
        roomTypeService.deleteRoomType(roomTypeId);
        return BoRoomTypeDto.Response.builder().updatedAt(LocalDateTime.now()).build();
    }

    public BoRoomTypeDetailDto.Response findRoomType(Long roomTypeId) {
        RoomType roomType = roomTypeService.findRoomType(roomTypeId);
        return BoRoomTypeDetailDto.Response.builder().id(roomType.getId()).name(roomType.getName()).build();
    }

    @Transactional
    public BoLifeStylePatternDto.Response saveLifeStylePattern(BoLifeStylePatternDto.Request request) {
        LifePattern lifePattern = lifeStyleService.saveLifePattern(LifePattern.builder().name(request.getName()).dtype(request.getType()).sort(request.getSort()).build());
        List<LifePatternInformation> lifePatternInformationList = request.getDetails().stream().map(item ->
                LifePatternInformation.builder().lifePattern(lifePattern).dvalue(item.getValues()).description(item.getDescription()).build()).toList();
        lifeStyleService.saveLifePatternInformation(lifePatternInformationList);
        return BoLifeStylePatternDto.Response.builder().updatedAt(LocalDateTime.now()).build();
    }

    public BoLifeStylePatternListDto.Response findLifeStylePatternList(Pageable pageable) {
        return lifeStyleService.findLifeStylePatternList(pageable);
    }

    public BoLifeStylePatternDetailDto.Response findLifeStylePattern(Long patternId) {
        return lifeStyleService.findLifeStylePattern(patternId);
    }

    @Transactional
    public BoLifeStylePatternDto.Response modifyLifeStylePattern(BoLifeStylePatternDto.Request request, Long patternId) {
        LifePattern lifePattern = lifeStyleService.findLifeStyle(patternId);
        lifeStyleService.deleteLifeInformationByPattern(lifePattern);
        request.getDetails().forEach(detail ->
                lifeStyleService.saveLifeInformation(LifePatternInformation.builder().lifePattern(lifePattern).dvalue(detail.getValues()).description(detail.getDescription()).build()));
        lifePattern.modifyLifePattern(request.getName(), request.getType(), request.getSort());
        return BoLifeStylePatternDto.Response.builder().updatedAt(LocalDateTime.now()).build();
    }

    @Transactional
    public BoLifeStylePatternDto.Response deleteLifeStylePattern(Long patternId) {
        lifeStyleService.deleteLifePattern(patternId);
        return BoLifeStylePatternDto.Response.builder().updatedAt(LocalDateTime.now()).build();
    }

    public BoVerificationApproveListDto.Response findVerificationApproves(Pageable pageable) {
        return BoVerificationApproveListDto.Response.builder().employeeAuth(authenticationService.findVerificationApproves(pageable)).build();
    }

    public BoVerificationCancelListDto.Response findVerificationCancels(Pageable pageable) {
        return BoVerificationCancelListDto.Response.builder().employeeAuth(authenticationService.findVerificationCancels(pageable)).build();
    }

    public BoVerificationWaitingListDto.Response findVerificationsList(Pageable pageable) {
        return BoVerificationWaitingListDto.Response.builder().employeeAuth(authenticationService.findVerificationsList(pageable)).build();
    }

    public BoVerificationWaitingDetailDto.Response findVerifications(Long id) {
        return authenticationService.findVerifications(id);
    }

    @Transactional
    public BoVerificationDto.Response saveVerifications(Long id) {
        authenticationService.saveVerifications(id);
        return BoVerificationDto.Response.builder().updatedAt(LocalDateTime.now()).build();
    }

    @Transactional
    public BoVerificationDto.Response deleteVerifications(Long id) {
        authenticationService.deleteVerifications(id);
        return BoVerificationDto.Response.builder().updatedAt(LocalDateTime.now()).build();
    }

    @Transactional
    public BoNoticeDto.Response saveNotice(BoNoticeDto.Request request, Long memberId) {
        Member member = memberService.findById(memberId).orElseThrow(() -> new BusinessException(AuthErrorCode.MEMBER_NOT_FOUND));
        notificationService.saveNotification(Notification.builder().member(member).title(request.getTitle()).contents(request.getContents()).build());
        return BoNoticeDto.Response.builder().updatedAt(LocalDateTime.now()).build();
    }

    public BoNoticeListDto.Response findNoticeList(Pageable pageable) {
        return BoNoticeListDto.Response.builder().notices(notificationService.findNotificationList(pageable)).build();
    }

    public BoNoticeDetailDto.Response findNotice(Long id) {
        return notificationService.findNotification(id);
    }

    @Transactional
    public BoNoticeDto.Response modifyNotice(BoNoticeDto.Request request, Long id, Long memberId) {
        Member member = memberService.findById(memberId).orElseThrow(() -> new BusinessException(AuthErrorCode.MEMBER_NOT_FOUND));
        Notification notification = notificationService.findNotificationById(id);
        notification.modifyNotification(request, member);
        return BoNoticeDto.Response.builder().updatedAt(LocalDateTime.now()).build();
    }

    @Transactional
    public BoNoticeDto.Response deleteNotice(Long id, Long memberId) {
        Member member = memberService.findById(memberId).orElseThrow(() -> new BusinessException(AuthErrorCode.MEMBER_NOT_FOUND));
        Notification notification = notificationService.findNotificationById(id);
        notification.deleteNotification(member);
        return BoNoticeDto.Response.builder().updatedAt(LocalDateTime.now()).build();
    }

    @Transactional
    public BoInquiryReplyDto.Response saveInquiryReply(BoInquiryReplyDto.Request request, Long memberId) {
        Member member = memberService.findById(memberId).orElseThrow(() -> new BusinessException(AuthErrorCode.MEMBER_NOT_FOUND));
        Inquiry inquiry = inquirieService.findInquiryById(request.getInquirieId());
        inquirieService.saveInquirieReply(InquiryComment.builder().member(member).inquiry(inquiry).contents(request.getContents()).build());
        return BoInquiryReplyDto.Response.builder().updatedAt(LocalDateTime.now()).build();
    }

    public BoInquiryListDto.Response findInquirieList(Pageable pageable, BoInquiryListDto.Request request) {
        List<BoInquiryListDto.Response.InquiryItem> inquiryItemList = inquirieService.findBackOfficeInquirieList(pageable, request);
        return BoInquiryListDto.Response.builder().inquiries(inquiryItemList).build();
    }

    public BoInquiryDetailDto.Response findInquirie(Long id) {
        return BoInquiryDetailDto.Response.builder().inquirie(inquirieService.findBackOfficeInquirie(id)).build();
    }

    public BoMemberListDto.Response findMemberList(Pageable pageable, BoMemberListDto.Request request) {
        return memberService.findBackOfficeMemberList(pageable, request);
    }

    public BoMemberDetailDto.Response findMember(Long id) {
        memberService.findById(id).orElseThrow(() -> new BusinessException(AuthErrorCode.MEMBER_NOT_FOUND));
        return memberService.findBackOfficeMember(id);
    }

    @Transactional
    public BoMemberCancelDto.Response deleteMember(Long id) {
        Member member = memberService.findById(id).orElseThrow(() -> new BusinessException(AuthErrorCode.MEMBER_NOT_FOUND));
        memberService.setMemberState(member, MemberState.INACTIVE);
        return BoMemberCancelDto.Response.builder().updatedAt(LocalDateTime.now()).build();
    }

    @Transactional
    public BoMemberAuthDto.Response authMember(Long id, BoMemberAuthDto.Request request) {
        Member member = memberService.findById(id).orElseThrow(() -> new BusinessException(AuthErrorCode.MEMBER_NOT_FOUND));
        memberService.setMemberAuth(member, request.getMemberRole());
        return BoMemberAuthDto.Response.builder().updatedAt(LocalDateTime.now()).build();
    }

    public BoReportWaitListDto.Response findReportWaitList(Pageable pageable) {
        return BoReportWaitListDto.Response.builder().reportInfoList(declarationService.findReportWaitList(pageable)).build();
    }

    public BoReportDoneListDto.Response findReportDoneList(Pageable pageable) {
        return BoReportDoneListDto.Response.builder().reportInfoList(declarationService.findReportDoneList(pageable)).build();
    }

    @Transactional
    public BoReportHiddenDto.Response reportHidden(BoReportHiddenDto.Request request) {
        declarationService.reportHidden(request.getId(), request.getType(), request.getReason());
        return BoReportHiddenDto.Response.builder().updatedAt(LocalDateTime.now()).build();
    }

    @Transactional
    public BoReportNoActionDto.Response reportNoAction(BoReportNoActionDto.Request request) {
        declarationService.reportNoAction(request.getId(), request.getType());
        return BoReportNoActionDto.Response.builder().updatedAt(LocalDateTime.now()).build();
    }

    @Transactional
    public BoReportSuspendedDto.Response reportSuspended(BoReportSuspendedDto.Request request) {
        declarationService.reportSuspended(request.getId(), request.getType(), request.getReason());
        return BoReportSuspendedDto.Response.builder().updatedAt(LocalDateTime.now()).build();
    }

    public BoBoardListDto.Response findBoardList(Pageable pageable, BoBoardListDto.Request request) {
        return BoBoardListDto.Response.builder().boardInfoList(roommateBoardService.findBackOfficeBoardList(pageable, request)).build();
    }

    public BoBoardDetailDto.Response findBoard(Long id) {
        return roommateBoardService.findBackOffcieBoard(id);
    }

    @Transactional
    public BoBoardDeleteDto.Response deleteBoard(Long id, BoBoardDeleteDto.Request request) {
        roommateBoardService.deleteBackOfficeBoard(id, request.getRejectReason());
        return BoBoardDeleteDto.Response.builder().updatedAt(LocalDateTime.now()).build();
    }

    public BoTypeTermsListDto.Response findTypeTermsList() {
        return BoTypeTermsListDto.Response.builder().termTypes(agreementService.findTypeTermsList()).build();
    }

    @Transactional
    public BoTypeTermsDto.Response modifyTermType(Long termTypeId, BoTypeTermsDto.Request request) {
        agreementService.findAgreementTypeById(termTypeId).modifyAgreementType(request.getTitle());
        return BoTypeTermsDto.Response.builder().updatedAt(LocalDateTime.now()).build();
    }

    @Transactional
    public BoTypeTermsDto.Response saveTermType(BoTypeTermsDto.Request request) {
        agreementService.saveTermType(AgreementType.builder().name(request.getTitle()).isDeleted(false).build());
        return BoTypeTermsDto.Response.builder().updatedAt(LocalDateTime.now()).build();
    }

    @Transactional
    public BoTypeTermsDto.Response deleteTermType(Long termTypeId) {
        agreementService.deleteTermType(termTypeId);
        return BoTypeTermsDto.Response.builder().updatedAt(LocalDateTime.now()).build();
    }
}
