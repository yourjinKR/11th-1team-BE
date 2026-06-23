package org.example.knockin.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.knockin.dto.*;
import org.example.knockin.entity.agreement.Agreement;
import org.example.knockin.entity.life.LifePattern;
import org.example.knockin.entity.life.LifePatternInformation;
import org.example.knockin.entity.room.RoomType;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BackOfficeServiceImpl {
    private final AgreementServiceImpl agreementService;
    private final RoomTypeServiceImpl roomTypeService;
    private final LifeStyleServiceImpl lifeStyleService;
    private final AuthenticationServiceImpl authenticationService;

    @Transactional
    public BoTermsDto.Response saveTerms(BoTermsDto.Request request) {
        agreementService.saveAgreement(Agreement.builder().title(request.getTitle()).contents(request.getContents()).isRequired(request.getIsRequired()).build());
        return BoTermsDto.Response.builder().updatedAt(LocalDateTime.now()).build();
    }

    @Transactional
    public BoTermsDto.Response modifyTerms(BoTermsDto.Request request, Long termsId) {
        agreementService.modifyTemporaryAgreement(Agreement.builder().title(request.getTitle()).contents(request.getContents()).isRequired(request.getIsRequired()).build(), termsId);
        return BoTermsDto.Response.builder().updatedAt(LocalDateTime.now()).build();
    }

    public BoTermsListDto.Response findTermsList(Pageable pageable) {
        List<BoTermsListDto.Response.TermsItem> termsItemList = agreementService.findAgreementList(pageable).stream().map(item ->
                BoTermsListDto.Response.TermsItem.builder().title(item.getTitle()).createAt(item.getCreatedAt()).id(item.getId()).build()).toList();
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
}
