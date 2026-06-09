package org.example.knockin.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.knockin.dto.*;
import org.example.knockin.entity.agreement.AgreementLog;
import org.example.knockin.entity.agreement.MemberAgreement;
import org.example.knockin.entity.life.*;
import org.example.knockin.entity.member.BasicInformation;
import org.example.knockin.entity.member.Member;
import org.example.knockin.entity.room.*;
import org.example.knockin.global.auth.exception.AuthErrorCode;
import org.example.knockin.global.exception.BusinessException;
import org.example.knockin.global.exception.MetaErrorCode;
import org.example.knockin.global.exception.OnBoardErrorCode;
import org.example.knockin.repository.agreement.MemberAgreementRepository;
import org.example.knockin.repository.life.*;
import org.example.knockin.repository.member.BasicInformationRepository;
import org.example.knockin.repository.room.OfferRoomTypeRepository;
import org.example.knockin.repository.room.RoomProfileRepository;
import org.example.knockin.repository.room.RoomSeekerProfileRegionRepository;
import org.example.knockin.repository.room.SeekerRoomTypeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.time.LocalDateTime;
import java.util.*;
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
    private final LifePatternInformationRepository lifePatternInformationRepository;
    private final PreferenceConditionRepository preferenceConditionRepository;
    private final PreferenceConditionLogRepository preferenceConditionLogRepository;
    private final PreferenceConditionWeightRepository preferenceConditionWeightRepository;
    private final PreferenceConditionWeightLogRepository preferenceConditionWeightLogRepository;

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

    @Transactional
    public void modifyLifeStyle(ModifyProfileLifeStyleDto.Request request, Member member) {
        List<Long> memberLifePatternIds = request.getLifestyles().stream()
                .map(ModifyProfileLifeStyleDto.Request.LifeStyleInfo::getId)
                .toList();

        List<Long> newLifestyleIds = request.getLifestyles().stream()
                .map(ModifyProfileLifeStyleDto.Request.LifeStyleInfo::getLifestyleId)
                .toList();

        Map<Long, LifePatternInformation> newInfoMap = lifePatternInformationRepository.findAllById(newLifestyleIds).stream()
                .collect(Collectors.toMap(LifePatternInformation::getId, info -> info));

        List<MemberLifePattern> memberLifePatternList = memberLifePatternRepository.findByMember(member);
        List<MemberLifePattern> modifyMemberLifePatternList = memberLifePatternList.stream()
                .filter(item -> memberLifePatternIds.contains(item.getId()))
                .toList();

        modifyMemberLifePatternList.forEach(item -> {
            request.getLifestyles().forEach(data -> {
                if (Objects.equals(data.getId(), item.getId())) {
                    LifePatternInformation newInfo = newInfoMap.get(data.getLifestyleId());

                    if(!Objects.equals(newInfo.getLifePattern().getId(), item.getLifePatternInformation().getLifePattern().getId())) {
                        throw new BusinessException(OnBoardErrorCode.ONBOARD_LIFE_STYLE_VAILDATION_FAIL);
                    }

                    item.modifyLifePatternInformation(newInfo);
                }
            });
        });
    }

    @Transactional
    public void modifyLifeStyleLog(Member member) {
        List<MemberLifePattern> memberLifePatternList = memberLifePatternRepository.findByMember(member);
        List<MemberLifePatternLog> logList = memberLifePatternList.stream().map(pattern ->
                MemberLifePatternLog.builder().member(member).lifePatternInformation(pattern.getLifePatternInformation()).build()).toList();

        if (!logList.isEmpty()) {
            memberLifePatternLogRepository.saveAll(logList);
        }
    }

    @Transactional
    public ModifyProfileLifeStyleDto.Response modifyLifeStyleLogic(ModifyProfileLifeStyleDto.Request request, Long memberId) {
        Member member = memberService.findById(memberId).orElseThrow(() -> new BusinessException(AuthErrorCode.MEMBER_NOT_FOUND));
        modifyLifeStyle(request, member);
        modifyLifeStyleLog(member);
        return ModifyProfileLifeStyleDto.Response.builder().updatedAt(LocalDateTime.now()).build();
    }

    @Transactional
    public void modifyRoomInfo(ModifyProfileRoomInfoDto.Request request, Member member) {
        RoomProfile roomProfile = roomProfileRepository.findByMember(member).getFirst();
        RoomProfileType targetType = request.getType();

        if (roomProfile.getType() != targetType) {
            if (roomProfile instanceof RoomOfferProfile roomOfferProfile) {
                offerRoomTypeRepository.deleteByRoomOfferProfile(roomOfferProfile);
            } else if (roomProfile instanceof RoomSeekerProfile seekerProfile) {
                seekerRoomTypeRepository.deleteByRoomSeekerProfile(seekerProfile);
                roomSeekerProfileRegionRepository.deleteByRoomSeekerProfile(seekerProfile);
            }

            roomProfileRepository.delete(roomProfile);
            roomProfileRepository.flush();

            if (targetType == RoomProfileType.SEEKER) {
                RoomSeekerProfile newSeekerProfile = roomProfileRepository.save(RoomSeekerProfile.builder()
                        .member(member)
                        .minDeposit(request.getMinDeposit())
                        .maxDeposit(request.getMaxDeposit())
                        .minMonthlyRent(request.getMinMounthRent())
                        .maxMonthlyRent(request.getMaxMounthRent())
                        .isComeableAtNegotiable(request.isComeableAtNegotiable())
                        .comeableAt(request.getComeEnableAt())
                        .build());

                List<RoomSeekerProfileRegion> seekerRegions = metaService.findByRegions(request.getRegion()).stream().map(region -> RoomSeekerProfileRegion.builder().roomSeekerProfile(newSeekerProfile).region(region).build()).toList();
                roomSeekerProfileRegionRepository.saveAll(seekerRegions);

                List<SeekerRoomType> seekerRoomTypes = metaService.findByRoomTypes(request.getRoomProfile()).stream().map(roomType -> SeekerRoomType.builder().roomSeekerProfile(newSeekerProfile).roomType(roomType).build()).toList();
                seekerRoomTypeRepository.saveAll(seekerRoomTypes);
            } else if (targetType == RoomProfileType.OFFER) {
                Region region = metaService.findByRegionId(request.getRegion().getFirst())
                        .orElseThrow(() -> new BusinessException(MetaErrorCode.REGION_NOT_FOUND));

                RoomOfferProfile newOfferProfile = roomProfileRepository.save(RoomOfferProfile.builder()
                        .member(member)
                        .region(region)
                        .deposit(request.getDeposit())
                        .monthlyRent(request.getMounthRent())
                        .isComeableAtNegotiable(request.isComeableAtNegotiable())
                        .comeableAt(request.getComeEnableAt())
                        .build());

                List<OfferRoomType> offerRoomTypes = metaService.findByRoomTypes(request.getRoomProfile()).stream().map(roomType -> OfferRoomType.builder().roomOfferProfile(newOfferProfile).roomType(roomType).build()).toList();
                offerRoomTypeRepository.saveAll(offerRoomTypes);
            }
        } else {
            List<RoomType> roomTypeList = metaService.findByRoomTypes(request.getRoomProfile());

            if (roomProfile instanceof RoomOfferProfile roomOfferProfile) {
                Region region = metaService.findByRegionId(request.getRegion().getFirst()).orElseThrow(() -> new BusinessException(MetaErrorCode.REGION_NOT_FOUND));

                roomOfferProfile.updateOffer(request, region);
                offerRoomTypeRepository.deleteByRoomOfferProfile(roomOfferProfile);

                List<OfferRoomType> offerRoomTypeList = roomTypeList.stream().map(item -> OfferRoomType.builder().roomType(item).roomOfferProfile(roomOfferProfile).build()).toList();
                offerRoomTypeRepository.saveAll(offerRoomTypeList);
            } else if (roomProfile instanceof RoomSeekerProfile seekerProfile) {
                List<Region> regionList = metaService.findByRegions(request.getRegion());

                seekerProfile.updateSeeker(request);
                seekerRoomTypeRepository.deleteByRoomSeekerProfile(seekerProfile);

                List<SeekerRoomType> seekerRoomTypes = roomTypeList.stream().map(item -> SeekerRoomType.builder().roomType(item).roomSeekerProfile(seekerProfile).build()).toList();
                seekerRoomTypeRepository.saveAll(seekerRoomTypes);

                roomSeekerProfileRegionRepository.deleteByRoomSeekerProfile(seekerProfile);

                List<RoomSeekerProfileRegion> roomSeekerProfileRegionList = regionList.stream().map(item -> RoomSeekerProfileRegion.builder().roomSeekerProfile(seekerProfile).region(item).build()).toList();
                roomSeekerProfileRegionRepository.saveAll(roomSeekerProfileRegionList);
            } else {
                throw new BusinessException(OnBoardErrorCode.ONBOARD_ROOM_INFO_VAILDATION_FAIL);
            }
        }
    }

    @Transactional
    public ModifyProfileRoomInfoDto.Response modifyRoomInfoLogic(ModifyProfileRoomInfoDto.Request request, Long memberId) {
        Member member = memberService.findById(memberId).orElseThrow(() -> new BusinessException(AuthErrorCode.MEMBER_NOT_FOUND));
        modifyRoomInfo(request, member);
        return ModifyProfileRoomInfoDto.Response.builder().updatedAt(LocalDateTime.now()).build();
    }

    @Transactional
    public ModifyProfileAllDto.Response modifyAll(ModifyProfileAllDto.Request request, Long memberId) {
        Member member = memberService.findById(memberId).orElseThrow(() -> new BusinessException(AuthErrorCode.MEMBER_NOT_FOUND));

        ModifyProfileBasicDto.Request basicRequest = ModifyProfileBasicDto.Request.builder()
                .name(request.getName())
                .birth(request.getBirth())
                .gender(request.getGender())
                .email(request.getEmail())
                .terms(request.getTerms())
                .build();

        List<ModifyProfileLifeStyleDto.Request.LifeStyleInfo> mappedLifestyles = request.getLifestyles().stream()
                .map(info -> {
                    ModifyProfileLifeStyleDto.Request.LifeStyleInfo targetInfo = new ModifyProfileLifeStyleDto.Request.LifeStyleInfo();
                    targetInfo.setId(info.getId());
                    targetInfo.setLifestyleId(info.getLifestyleId());
                    return targetInfo;
                }).toList();

        ModifyProfileLifeStyleDto.Request lifeStyleRequest = ModifyProfileLifeStyleDto.Request.builder()
                .lifestyles(mappedLifestyles)
                .build();

        ModifyProfileRoomInfoDto.Request roomInfoRequest = ModifyProfileRoomInfoDto.Request.builder()
                .type(request.getType())
                .minDeposit(request.getMinDeposit())
                .maxDeposit(request.getMaxDeposit())
                .minMounthRent(request.getMinMounthRent())
                .maxMounthRent(request.getMaxMounthRent())
                .comeEnableAt(request.getComeEnableAt())
                .isComeableAtNegotiable(request.isComeableAtNegotiable())
                .region(request.getRegion())
                .roomProfile(request.getRoomProfile())
                .deposit(request.getDeposit())
                .mounthRent(request.getMounthRent())
                .build();

        modifyBasicInfo(basicRequest, member);
        modifyAgreement(basicRequest, member);
        modifyLifeStyle(lifeStyleRequest, member);
        modifyLifeStyleLog(member);
        modifyRoomInfo(roomInfoRequest, member);

        return ModifyProfileAllDto.Response.builder().updatedAt(LocalDateTime.now()).build();
    }

    @Transactional
    public List<PreferenceCondition> savePreferenceLifeStyle(SavePreferencesLifeStyleDto.Request request, Member member) {
        List<PreferenceCondition> preferenceConditionList = new ArrayList<>();
        metaService.findByLifeStyle(request.getLifestyles()).forEach(item ->
                preferenceConditionList.add(PreferenceCondition.builder().member(member).lifePatternInformation(item).build()));
        return preferenceConditionRepository.saveAll(preferenceConditionList);
    }

    @Transactional
    public List<PreferenceConditionLog> savePreferenceLifeStyleLog(SavePreferencesLifeStyleDto.Request request, Member member) {
        List<PreferenceConditionLog> preferenceConditionLogList = new ArrayList<>();
        metaService.findByLifeStyle(request.getLifestyles()).forEach(item ->
                preferenceConditionLogList.add(PreferenceConditionLog.builder().member(member).lifePatternInformation(item).build()));
        return preferenceConditionLogRepository.saveAll(preferenceConditionLogList);
    }

    @Transactional
    public SavePreferencesLifeStyleDto.Response savePreferenceLifeStyleLogic(SavePreferencesLifeStyleDto.Request request, Long memberId) {
        Member member = memberService.findById(memberId).orElseThrow(() -> new BusinessException(AuthErrorCode.MEMBER_NOT_FOUND));

        if(savePreferenceLifeStyle(request, member).isEmpty()) throw new BusinessException(OnBoardErrorCode.ONBOARD_PREFERENCE_STEP1_SAVE_ERROR);
        if(savePreferenceLifeStyleLog(request, member).isEmpty()) throw new BusinessException(OnBoardErrorCode.ONBOARD_PREFERENCE_STEP1_LOG_SAVE_ERROR);

        return SavePreferencesLifeStyleDto.Response.builder().updatedAt(LocalDateTime.now()).build();
    }

    @Transactional
    public List<PreferenceConditionWeight>  savePreferenceCondition(SavePreferencesConditionsDto.Request request, Member member) {
        List<PreferenceConditionWeight> preferenceConditionWeightList = new ArrayList<>();
        metaService.findLifePatternByLifeStyle(request.getConditions()).forEach(item ->
                preferenceConditionWeightList.add(PreferenceConditionWeight.builder().member(member).lifePattern(item).build()));
        return preferenceConditionWeightRepository.saveAll(preferenceConditionWeightList);
    }

    @Transactional
    public List<PreferenceConditionWeightLog>  savePreferenceConditionLog(SavePreferencesConditionsDto.Request request, Member member) {
        List<PreferenceConditionWeightLog> preferenceConditionWeightLogList = new ArrayList<>();
        metaService.findLifePatternByLifeStyle(request.getConditions()).forEach(item ->
                preferenceConditionWeightLogList.add(PreferenceConditionWeightLog.builder().member(member).lifePattern(item).build()));
        return preferenceConditionWeightLogRepository.saveAll(preferenceConditionWeightLogList);
    }

    @Transactional
    public SavePreferencesConditionsDto.Response savePreferenceConditionLogic(SavePreferencesConditionsDto.Request request, Long memberId) {
        Member member = memberService.findById(memberId).orElseThrow(() -> new BusinessException(AuthErrorCode.MEMBER_NOT_FOUND));

        if(savePreferenceCondition(request, member).isEmpty()) throw new BusinessException(OnBoardErrorCode.ONBOARD_PREFERENCE_STEP2_SAVE_ERROR);
        if(savePreferenceConditionLog(request, member).isEmpty()) throw new BusinessException(OnBoardErrorCode.ONBOARD_PREFERENCE_STEP2_LOG_SAVE_ERROR);

        return SavePreferencesConditionsDto.Response.builder().updatedAt(LocalDateTime.now()).build();
    }

    @Transactional
    public SavePreferencesAllDto.Response savePreferenceAll(SavePreferencesAllDto.Request request, Long memberId) {
        Member member = memberService.findById(memberId).orElseThrow(() -> new BusinessException(AuthErrorCode.MEMBER_NOT_FOUND));

        SavePreferencesLifeStyleDto.Request lifeStyleRequest = new SavePreferencesLifeStyleDto.Request(request.getLifestyles());
        SavePreferencesConditionsDto.Request conditionRequest = new SavePreferencesConditionsDto.Request(request.getConditions());

        if(savePreferenceLifeStyle(lifeStyleRequest, member).isEmpty()) throw new BusinessException(OnBoardErrorCode.ONBOARD_PREFERENCE_STEP1_SAVE_ERROR);
        if(savePreferenceLifeStyleLog(lifeStyleRequest, member).isEmpty()) throw new BusinessException(OnBoardErrorCode.ONBOARD_PREFERENCE_STEP1_LOG_SAVE_ERROR);
        if(savePreferenceCondition(conditionRequest, member).isEmpty()) throw new BusinessException(OnBoardErrorCode.ONBOARD_PREFERENCE_STEP2_SAVE_ERROR);
        if(savePreferenceConditionLog(conditionRequest, member).isEmpty()) throw new BusinessException(OnBoardErrorCode.ONBOARD_PREFERENCE_STEP2_LOG_SAVE_ERROR);

        return SavePreferencesAllDto.Response.builder().updatedAt(LocalDateTime.now()).build();
    }
}
