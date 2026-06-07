package org.example.knockin.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.knockin.dto.*;
import org.example.knockin.entity.agreement.AgreementLog;
import org.example.knockin.entity.agreement.MemberAgreement;
import org.example.knockin.entity.life.MemberLifePattern;
import org.example.knockin.entity.life.MemberLifePatternLog;
import org.example.knockin.entity.member.BasicInformation;
import org.example.knockin.entity.member.Member;
import org.example.knockin.entity.room.*;
import org.example.knockin.global.auth.exception.AuthErrorCode;
import org.example.knockin.global.exception.BusinessException;
import org.example.knockin.global.exception.MetaErrorCode;
import org.example.knockin.global.exception.OnBoardErrorCode;
import org.example.knockin.repository.agreement.MemberAgreementRepository;
import org.example.knockin.repository.life.MemberLifePatternLogRepository;
import org.example.knockin.repository.life.MemberLifePatternRepository;
import org.example.knockin.repository.member.BasicInformationRepository;
import org.example.knockin.repository.room.OfferRoomTypeRepository;
import org.example.knockin.repository.room.RoomProfileRepository;
import org.example.knockin.repository.room.RoomSeekerProfileRegionRepository;
import org.example.knockin.repository.room.SeekerRoomTypeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OnBoardingServiceImpl {
    private final BasicInformationRepository basicInformationRepository;
    private final MemberServiceImpl memberService;
    private final MemberAgreementRepository memberAgreementRepository;
    private final MemberLifePatternRepository memberLifePatternRepository;
    private final MemberLifePatternLogRepository memberLifePatternLogRepository;
    private final RoomProfileRepository roomProfileRepository;
    private final OfferRoomTypeRepository offerRoomTypeRepository;
    private final SeekerRoomTypeRepository seekerRoomTypeRepository;
    private final RoomSeekerProfileRegionRepository roomSeekerProfileRegionRepository;
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

    @Transactional
    public List<MemberLifePattern> saveMemberLifeStyle(SaveProfileLifeStyleDto.Request request, Member member) {
        List<MemberLifePattern> memberLifePatternList = new ArrayList<>();
        List<MemberLifePatternLog> memberLifePatternLogList = new ArrayList<>();

        metaService.findByLifeStyle(request.getLifestyles()).forEach(item -> {
            memberLifePatternList.add(MemberLifePattern.builder().member(member).lifePatternInformation(item).build());
            memberLifePatternLogList.add(MemberLifePatternLog.builder().member(member).lifePatternInformation(item).build());
        });


        memberLifePatternLogRepository.saveAll(memberLifePatternLogList);
        return memberLifePatternRepository.saveAll(memberLifePatternList);
    }

    @Transactional
    public SaveProfileLifeStyleDto.Response saveLifeStyleLogic(SaveProfileLifeStyleDto.Request request, Long memberId) {
        Member member = memberService.findById(memberId).orElseThrow(() -> new BusinessException(AuthErrorCode.MEMBER_NOT_FOUND));
        if(saveMemberLifeStyle(request, member).isEmpty()) throw new BusinessException(OnBoardErrorCode.ONBOARD_LIFE_STYLE_SAVE_ERROR);
        return SaveProfileLifeStyleDto.Response.builder().updatedAt(LocalDateTime.now()).build();
    }

    @Transactional
    public RoomProfile saveRoomInfo(SaveProfileRoomInfoDto.Request request, Member member) {
        RoomProfile roomProfile = null;

        switch (request.getType()) {
            case OFFER -> {
                List<OfferRoomType> offerRoomTypeList = new ArrayList<>();

                Region region = metaService.findByRegionId(request.getRegion().getFirst()).orElseThrow(() -> new BusinessException(MetaErrorCode.REGION_NOT_FOUND));
                RoomOfferProfile roomOfferProfile = roomProfileRepository.save(RoomOfferProfile.builder()
                        .member(member)
                        .region(region)
                        .deposit(request.getDeposit())
                        .monthlyRent(request.getMounthRent())
                        .isComeableAtNegotiable(request.isComeableAtNegotiable())
                        .comeableAt(request.getComeEnableAt()).build());
                roomProfile = roomOfferProfile;

                metaService.findByRoomTypes(request.getRoomProfile()).forEach(item -> {
                    offerRoomTypeList.add(OfferRoomType.builder().roomType(item).roomOfferProfile(roomOfferProfile).build());
                });
                offerRoomTypeRepository.saveAll(offerRoomTypeList);
            }
            case SEEKER -> {
                List<SeekerRoomType> seekerRoomTypes = new ArrayList<>();
                List<RoomSeekerProfileRegion> roomSeekerProfileRegionList = new ArrayList<>();

                RoomSeekerProfile roomSeekerProfile = roomProfileRepository.save(RoomSeekerProfile.builder()
                        .member(member)
                        .minDeposit(request.getMinDeposit())
                        .maxDeposit(request.getMaxDeposit())
                        .minMonthlyRent(request.getMinMounthRent())
                        .maxMonthlyRent(request.getMaxMounthRent())
                        .isComeableAtNegotiable(request.isComeableAtNegotiable())
                        .comeableAt(request.getComeEnableAt()).build());
                roomProfile = roomSeekerProfile;

                metaService.findByRegions(request.getRegion()).forEach(item -> {
                    roomSeekerProfileRegionList.add(RoomSeekerProfileRegion.builder().roomSeekerProfile(roomSeekerProfile).region(item).build());
                });
                roomSeekerProfileRegionRepository.saveAll(roomSeekerProfileRegionList);

                metaService.findByRoomTypes(request.getRoomProfile()).forEach(item -> {
                    seekerRoomTypes.add(SeekerRoomType.builder().roomType(item).roomSeekerProfile(roomSeekerProfile).build());
                });
                seekerRoomTypeRepository.saveAll(seekerRoomTypes);
            }
        }

        return roomProfile;
    }

    @Transactional
    public SaveProfileRoomInfoDto.Response saveRoomInfoLogic(SaveProfileRoomInfoDto.Request request, Long memberId) {
        Member member = memberService.findById(memberId).orElseThrow(() -> new BusinessException(AuthErrorCode.MEMBER_NOT_FOUND));
        if(ObjectUtils.isEmpty(saveRoomInfo(request,member))) throw new BusinessException(OnBoardErrorCode.ONBOARD_ROOM_INFO_SAVE_ERROR);
        return SaveProfileRoomInfoDto.Response.builder().updatedAt(LocalDateTime.now()).build();
    }

    @Transactional
    public SaveProfileAllDto.Response saveAll(SaveProfileAllDto.Request request, Long memberId) {
        Member member = memberService.findById(memberId).orElseThrow(() -> new BusinessException(AuthErrorCode.MEMBER_NOT_FOUND));

        SaveProfileBasicDto.Request basicRequest = SaveProfileBasicDto.Request.builder()
                .name(request.getName())
                .birth(request.getBirth())
                .gender(request.getGender())
                .email(request.getEmail())
                .terms(request.getTerms())
                .build();

        SaveProfileLifeStyleDto.Request lifeStyleRequest = SaveProfileLifeStyleDto.Request.builder()
                .lifestyles(request.getLifestyles())
                .build();

        SaveProfileRoomInfoDto.Request roomInfoRequest = SaveProfileRoomInfoDto.Request.builder()
                .type(request.getType())
                .minDeposit(request.getMinDeposit())
                .maxDeposit(request.getMaxDeposit())
                .minMounthRent(request.getMinMounthRent())
                .maxMounthRent(request.getMaxMounthRent())
                .comeEnableAt(request.getComeEnableAt())
                .region(request.getRegion())
                .roomProfile(request.getRoomProfile())
                .deposit(request.getDeposit())
                .mounthRent(request.getMounthRent())
                .isComeableAtNegotiable(request.isComeableAtNegotiable())
                .build();

        if(ObjectUtils.isEmpty(saveBasicInfo(basicRequest, member))) throw new BusinessException(OnBoardErrorCode.ONBOARD_BASIC_SAVE_ERROR);
        if(saveMemberAgreement(basicRequest, member).isEmpty()) throw new BusinessException(OnBoardErrorCode.ONBOARD_TERM_SAVE_ERROR);
        if(saveMemberLifeStyle(lifeStyleRequest, member).isEmpty()) throw new BusinessException(OnBoardErrorCode.ONBOARD_LIFE_STYLE_SAVE_ERROR);
        if(ObjectUtils.isEmpty(saveRoomInfo(roomInfoRequest,member))) throw new BusinessException(OnBoardErrorCode.ONBOARD_ROOM_INFO_SAVE_ERROR);

        return SaveProfileAllDto.Response.builder().updatedAt(LocalDateTime.now()).build();
    }

    @Transactional
    public void modifyBasicInfo(ModifyProfileBasicDto.Request request, Member member) {
        BasicInformation basicInformation = basicInformationRepository.findByMember(member).getFirst();
        basicInformation.modifyBasicInformation(request);
    }

    @Transactional
    public void modifyAgreement(ModifyProfileBasicDto.Request request, Member member) {
        List<AgreementLog> requestAgreementList = metaService.findByAgreementLogIsCurrent(request.getTerms());
        List<AgreementLog> memberAgreementList = memberAgreementRepository.findByMember(member).stream().map(MemberAgreement::getAgreementLog).toList();

        Set<Long> memberAgreementIds = memberAgreementList.stream().map(AgreementLog::getId).collect(Collectors.toSet());
        List<AgreementLog> skipList = requestAgreementList.stream().filter(reqLog -> memberAgreementIds.contains(reqLog.getId())).toList();

        if (skipList.isEmpty()) {
            memberAgreementRepository.findByMember(member).forEach(MemberAgreement::disableAgree);
        } else {
            memberAgreementRepository.findByMemberAndAgreementLogNotIn(member, skipList).forEach(MemberAgreement::disableAgree);
        }

        requestAgreementList.forEach(item -> {
            boolean isNotInSkipList = skipList.stream().noneMatch(skipItem -> Objects.equals(skipItem.getId(), item.getId()));

            if (isNotInSkipList) {
                memberAgreementRepository.save(MemberAgreement.builder()
                        .member(member)
                        .agreementLog(item)
                        .isAgreed(true)
                        .build());
            }
        });
    }

    @Transactional
    public ModifyProfileBasicDto.Response modifyBasicInfoLogic(ModifyProfileBasicDto.Request request, Long memberId) {
        Member member = memberService.findById(memberId).orElseThrow(() -> new BusinessException(AuthErrorCode.MEMBER_NOT_FOUND));

        modifyBasicInfo(request, member);
        modifyAgreement(request, member);

        return ModifyProfileBasicDto.Response.builder().updatedAt(LocalDateTime.now()).build();
    }
}
