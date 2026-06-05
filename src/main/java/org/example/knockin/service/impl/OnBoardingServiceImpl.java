package org.example.knockin.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.knockin.dto.SaveProfileBasicDto;
import org.example.knockin.entity.agreement.Agreement;
import org.example.knockin.entity.agreement.MemberAgreement;
import org.example.knockin.entity.member.BasicInformation;
import org.example.knockin.entity.member.Member;
import org.example.knockin.global.auth.exception.AuthErrorCode;
import org.example.knockin.global.exception.BusinessException;
import org.example.knockin.global.exception.OnBoardErrorCode;
import org.example.knockin.repository.agreement.MemberAgreementRepository;
import org.example.knockin.repository.member.BasicInformationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OnBoardingServiceImpl {
    private final BasicInformationRepository basicInformationRepository;
    private final MemberServiceImpl memberService;
    private final MemberAgreementRepository memberAgreementRepository;
    private final MetaServiceImpl metaService;

    @Transactional
    public BasicInformation saveBasicInfo(SaveProfileBasicDto.Request request, Member member) {
        BasicInformation basicInformation = BasicInformation.builder().member(member).name(request.getName()).birth(request.getBirth()).gender(request.getGender()).email(request.getEmail()).build();
        return basicInformationRepository.save(basicInformation);
    }

    @Transactional
    public List<MemberAgreement> saveMemberAgreement(SaveProfileBasicDto.Request request, Member member) {
        List<MemberAgreement> memberAgreementList = new ArrayList<>();

        metaService.findByAgreementLogIsCurrent(request.getTerms()).forEach(item -> {
            memberAgreementList.add(MemberAgreement.builder().member(member).agreementLog(item).isAgreed(true).build());
        });

        return memberAgreementRepository.saveAll(memberAgreementList);
    }

    @Transactional
    public SaveProfileBasicDto.Response saveBasicInfoLogic(SaveProfileBasicDto.Request request, Long memberId) {
        Member member = memberService.findById(memberId).orElseThrow(() -> new BusinessException(AuthErrorCode.MEMBER_NOT_FOUND));
        if(ObjectUtils.isEmpty(saveBasicInfo(request, member))) throw new BusinessException(OnBoardErrorCode.ONBOARD_BASIC_SAVE_ERROR);
        if(saveMemberAgreement(request, member).isEmpty()) throw new BusinessException(OnBoardErrorCode.ONBOARD_TERM_SAVE_ERROR);
        return SaveProfileBasicDto.Response.builder().updatedAt(LocalDateTime.now()).build();
    }
}
