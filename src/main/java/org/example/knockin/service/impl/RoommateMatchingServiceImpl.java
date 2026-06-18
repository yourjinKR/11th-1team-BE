package org.example.knockin.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.example.knockin.dto.MatchDetailDto;
import org.example.knockin.dto.MatchDto;
import org.example.knockin.dto.MatchListDto;
import org.example.knockin.dto.MemberReportDto;
import org.example.knockin.entity.auth.AuthenticationType;
import org.example.knockin.entity.member.Member;
import org.example.knockin.entity.member.MemberDeclaration;
import org.example.knockin.entity.member.MemberInterest;
import org.example.knockin.entity.room.RoomProfileType;
import org.example.knockin.global.exception.BusinessException;
import org.example.knockin.global.exception.MemberErrorCode;
import org.example.knockin.global.util.DateUtils;
import org.example.knockin.global.util.HasMemberId;
import org.example.knockin.global.util.StringUtils;
import org.example.knockin.repository.auth.AuthenticationRepository;
import org.example.knockin.repository.life.MemberLifePatternRepository;
import org.example.knockin.repository.life.PreferenceConditionRepository;
import org.example.knockin.repository.life.PreferenceConditionWeightRepository;
import org.example.knockin.repository.life.row.MatchingLifestyleRow;
import org.example.knockin.repository.life.row.MatchingPreferenceConditionRow;
import org.example.knockin.repository.life.row.MatchingPreferenceConditionWeightRow;
import org.example.knockin.repository.member.MemberDeclarationRepository;
import org.example.knockin.repository.member.MemberInterestRepository;
import org.example.knockin.repository.member.MemberRepository;
import org.example.knockin.repository.member.row.MatchingBasicInfoRow;
import org.example.knockin.repository.room.RoomOfferProfileRepository;
import org.example.knockin.repository.room.RoomSeekerProfileRepository;
import org.example.knockin.repository.room.row.MatchingOfferProfileRow;
import org.example.knockin.repository.room.row.MatchingSeekerProfileRow;
import org.example.knockin.repository.room.row.MatchingSeekerRegionRow;
import org.example.knockin.repository.room.row.MatchingSeekerRoomTypeRow;
import org.example.knockin.service.RoommateMatchingService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RoommateMatchingServiceImpl implements RoommateMatchingService {
    private final MemberRepository memberRepository;
    private final MemberInterestRepository memberInterestRepository;
    private final RoomSeekerProfileRepository roomSeekerProfileRepository;
    private final RoomOfferProfileRepository roomOfferProfileRepository;
    private final MemberLifePatternRepository memberLifePatternRepository;
    private final PreferenceConditionRepository preferenceConditionRepository;
    private final PreferenceConditionWeightRepository preferenceConditionWeightRepository;
    private final AuthenticationRepository authenticationRepository;
    private final MemberDeclarationRepository memberDeclarationRepository;

    @Override
    @Transactional(readOnly = true)
    public Slice<MatchListDto.Response> findMatchingList(Long memberId, MatchListDto.Request request) {
        int size = request.getSize();
        List<Long> excludeMemberIds = resolveExcludeMemberIds(memberId, request);

        List<MatchingBasicInfoRow> rawRows = memberRepository.findMatchingBasicRow(excludeMemberIds, size + 1);
        boolean hasNext = rawRows.size() > size;
        List<MatchingBasicInfoRow> matchingListBasicRow = rawRows.stream().limit(size).toList();
        List<Long> memberIds = matchingListBasicRow.stream().map(MatchingBasicInfoRow::memberId).toList();

        if (memberIds.isEmpty()) {
            return new SliceImpl<>(List.of(), PageRequest.of(0, size), false);
        }

        List<MatchingOfferProfileRow> matchingOfferProfileRows = roomOfferProfileRepository.findAllOfferProfileByMemberIdIn(memberIds);

        List<MatchingSeekerProfileRow> matchingSeekerProfileRows = roomSeekerProfileRepository.findAllSeekerProfileByMemberIdIn(memberIds);
        List<MatchingSeekerRegionRow> matchingSeekerRegionRows = roomSeekerProfileRepository.findAllSeekerRegionByMemberIdIn(memberIds);
        List<MatchingSeekerRoomTypeRow> matchingSeekerRoomTypeRows = roomSeekerProfileRepository.findAllSeekerRoomTypeByMemberIdIn(memberIds);

        List<MatchingLifestyleRow> matchingLifestyleRows = memberLifePatternRepository.findAllLifestyleByMemberIdIn(memberIds);
        List<MatchingPreferenceConditionRow> matchingPreferenceConditionRows = preferenceConditionRepository.findAllPreferenceConditionByMemberIdIn(memberIds);
        List<MatchingPreferenceConditionWeightRow> matchingPreferenceConditionWeightRows = preferenceConditionWeightRepository.findAllPreferenceConditionWeightByMemberIdIn(memberIds);

        Map<Long, MatchingOfferProfileRow> offerMap = HasMemberId.toMapByMemberId(matchingOfferProfileRows);
        Map<Long, MatchingSeekerProfileRow> seekerMap = HasMemberId.toMapByMemberId(matchingSeekerProfileRows);

        Map<Long, List<String>> seekerRegionMap = HasMemberId.groupingByMemberId(
                matchingSeekerRegionRows,
                row -> StringUtils.parseToRegionFullName(
                        row.grandParentRegionName(),
                        row.parentRegionName(),
                        row.regionName()
                )
        );

        Map<Long, List<String>> seekerRoomTypeMap = HasMemberId.groupingByMemberId(
                matchingSeekerRoomTypeRows,
                MatchingSeekerRoomTypeRow::roomTypeName
        );

        Map<Long, List<MatchListDto.Lifestyle>> lifestyleMap = HasMemberId.groupingByMemberId(
                matchingLifestyleRows,
                this::toLifestyle
        );

        Map<Long, List<MatchListDto.Condition>> conditionMap = HasMemberId.groupingByMemberId(
                matchingPreferenceConditionRows,
                this::toCondition
        );

        Map<Long, List<MatchListDto.ConditionWeight>> conditionWeightMap = HasMemberId.groupingByMemberId(
                matchingPreferenceConditionWeightRows,
                this::toConditionWeight
        );

        Set<Long> likedMemberIds = findLikedMemberIds(memberId, memberIds);

        List<MatchListDto.Response> responses = matchingListBasicRow.stream()
                .map(row -> toResponse(
                        row,
                        offerMap,
                        seekerMap,
                        seekerRegionMap,
                        seekerRoomTypeMap,
                        lifestyleMap,
                        conditionMap,
                        conditionWeightMap,
                        likedMemberIds
                ))
                .toList();

        return new SliceImpl<>(
                responses,
                PageRequest.of(0, size),
                hasNext
        );
    }

    private List<Long> resolveExcludeMemberIds(Long memberId, MatchListDto.Request request) {
        List<Long> excludeMemberIds = new ArrayList<>();

        if (request != null && request.getExcludeMemberIds() != null) {
            excludeMemberIds.addAll(request.getExcludeMemberIds());
        }

        if (memberId != null) {
            excludeMemberIds.add(memberId);
        }

        return excludeMemberIds.stream()
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }

    private Set<Long> findLikedMemberIds(Long memberId, List<Long> memberIds) {
        if (memberId == null || memberIds.isEmpty()) {
            return Set.of();
        }

        return Set.copyOf(memberInterestRepository.findActiveReceiverIdsBySenderIdAndReceiverIds(memberId, memberIds));
    }

    private MatchListDto.Lifestyle toLifestyle(MatchingLifestyleRow row) {
        return MatchListDto.Lifestyle.builder()
                .lifestyleId(row.lifestyleId())
                .name(row.name())
                .value(row.value())
                .description(row.description())
                .type(row.type())
                .build();
    }

    private MatchListDto.Condition toCondition(MatchingPreferenceConditionRow row) {
        return MatchListDto.Condition.builder()
                .conditionId(row.conditionId())
                .name(row.name())
                .value(row.value())
                .description(row.description())
                .type(row.type())
                .build();
    }

    private MatchListDto.ConditionWeight toConditionWeight(MatchingPreferenceConditionWeightRow row) {
        return MatchListDto.ConditionWeight.builder()
                .conditionWeightId(row.conditionWeightId())
                .name(row.name())
                .build();
    }

    private MatchListDto.Response toResponse(
            MatchingBasicInfoRow row,
            Map<Long, MatchingOfferProfileRow> offerMap,
            Map<Long, MatchingSeekerProfileRow> seekerMap,
            Map<Long, List<String>> seekerRegionMap,
            Map<Long, List<String>> seekerRoomTypeMap,
            Map<Long, List<MatchListDto.Lifestyle>> lifestyleMap,
            Map<Long, List<MatchListDto.Condition>> conditionMap,
            Map<Long, List<MatchListDto.ConditionWeight>> conditionWeightMap,
            Set<Long> likedMemberIds
    ) {
        Long candidateId = row.memberId();

        MatchListDto.OfferProfile offerProfile = null;
        MatchListDto.SeekerProfile seekerProfile = null;

        if (row.roomProfileType() == RoomProfileType.OFFER) {
            MatchingOfferProfileRow offerProfileRow = offerMap.get(candidateId);
            offerProfile = toOfferProfile(offerProfileRow);
        }

        if (row.roomProfileType() == RoomProfileType.SEEKER) {
            MatchingSeekerProfileRow matchingSeekerProfileRow = seekerMap.get(candidateId);
            List<String> regionFullNames = seekerRegionMap.getOrDefault(candidateId, List.of());
            List<String> roomTypeNames = seekerRoomTypeMap.getOrDefault(candidateId, List.of());
            seekerProfile = toSeekerProfile(matchingSeekerProfileRow, roomTypeNames, regionFullNames);
        }

        return MatchListDto.Response.builder()
                .memberId(candidateId)
                .memberProfileImageUrl(row.memberProfileImageUrl())
                .memberName(row.memberName())
                .memberAge(DateUtils.calculateAge(row.birth()))
                .gender(row.gender())
                .isLike(likedMemberIds.contains(candidateId))
                .roomProfileType(row.roomProfileType())
                .offerProfile(offerProfile)
                .seekerProfile(seekerProfile)
                // TODO: 계산식 확정 후 변경
                .score(null)
                .lifeStyles(lifestyleMap.getOrDefault(candidateId, List.of()))
                .conditions(conditionMap.getOrDefault(candidateId, List.of()))
                .conditionWeights(conditionWeightMap.getOrDefault(candidateId, List.of()))
                .build();
    }

    private MatchListDto.OfferProfile toOfferProfile(MatchingOfferProfileRow row) {
        if (row == null) {
            return null;
        }

        String regionFullName = StringUtils.parseToRegionFullName(
                row.grandParentRegionName(),
                row.parentRegionName(),
                row.regionName()
        );

        return MatchListDto.OfferProfile.builder()
                .deposit(row.deposit())
                .monthlyRent(row.monthlyRent())
                .regionFullName(regionFullName)
                .roomTypeName(row.roomTypeName())
                .build();
    }

    private MatchListDto.SeekerProfile toSeekerProfile(MatchingSeekerProfileRow row, List<String> roomTypeNames, List<String> regionFullNames) {
        if (row == null) {
            return null;
        }

        return MatchListDto.SeekerProfile.builder()
                .minDeposit(row.minDeposit())
                .maxDeposit(row.maxDeposit())
                .minMonthlyRent(row.minMonthlyRent())
                .maxMonthlyRent(row.maxMonthlyRent())
                .roomTypeNames(roomTypeNames)
                .regionFullNames(regionFullNames)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public MatchDetailDto.Response findMatchingDetail(Long targetMemberId, Long requesterId) {
        MatchingBasicInfoRow basicInfoRow = memberRepository.findMatchingBasicRowById(targetMemberId)
                .orElseThrow(() -> new BusinessException(MemberErrorCode.MEMBER_NOT_FOUND));

        List<AuthenticationType> authenticationTypes = authenticationRepository.getAcceptedAuthenticationTypeByMemberId(targetMemberId);

        RoomProfileType roomProfileType = basicInfoRow.roomProfileType();
        MatchingOfferProfileRow offerProfileRow = null;
        MatchingSeekerProfileRow seekerProfileRow = null;
        List<MatchingSeekerRegionRow> seekerRegionRows = List.of();
        List<MatchingSeekerRoomTypeRow> seekerRoomTypeRows = List.of();

        if (roomProfileType == RoomProfileType.OFFER) {
            offerProfileRow = roomOfferProfileRepository.findAllOfferProfileByMemberIdIn(List.of(targetMemberId))
                    .stream()
                    .findFirst()
                    .orElse(null);
        }

        if (roomProfileType == RoomProfileType.SEEKER) {
            seekerProfileRow = roomSeekerProfileRepository.findAllSeekerProfileByMemberIdIn(List.of(targetMemberId))
                    .stream()
                    .findFirst()
                    .orElse(null);
            seekerRegionRows = roomSeekerProfileRepository.findAllSeekerRegionByMemberIdIn(List.of(targetMemberId));
            seekerRoomTypeRows = roomSeekerProfileRepository.findAllSeekerRoomTypeByMemberIdIn(List.of(targetMemberId));
        }

        List<MatchingLifestyleRow> lifestyleRows = memberLifePatternRepository.findAllLifestyleByMemberIdIn(List.of(targetMemberId));
        List<MatchingPreferenceConditionRow> preferenceConditionRows = preferenceConditionRepository.findAllPreferenceConditionByMemberIdIn(List.of(targetMemberId));
        List<MatchingPreferenceConditionWeightRow> preferenceConditionWeightRows = preferenceConditionWeightRepository.findAllPreferenceConditionWeightByMemberIdIn(List.of(targetMemberId));

        boolean isLike = requesterId != null && memberInterestRepository.existsBySenderIdAndReceiverId(requesterId, targetMemberId);

        List<String> roomTypeNames = seekerRoomTypeRows.stream().map(MatchingSeekerRoomTypeRow::roomTypeName).toList();
        List<String> regionFullNames = seekerRegionRows.stream()
                .map(row -> StringUtils.parseToRegionFullName(
                        row.grandParentRegionName(),
                        row.parentRegionName(),
                        row.regionName()
                ))
                .toList();

        return MatchDetailDto.Response.builder()
                .memberId(basicInfoRow.memberId())
                .memberProfileImageUrl(basicInfoRow.memberProfileImageUrl())
                .memberName(basicInfoRow.memberName())
                .memberAge(DateUtils.calculateAge(basicInfoRow.birth()))
                .gender(basicInfoRow.gender())
                .isLike(isLike)
                .roomProfileType(roomProfileType)
                .offerProfile(roomProfileType == RoomProfileType.OFFER ? toOfferProfile(offerProfileRow) : null)
                .seekerProfile(roomProfileType == RoomProfileType.SEEKER ? toSeekerProfile(seekerProfileRow, roomTypeNames, regionFullNames) : null)
                .lifeStyles(lifestyleRows.stream().map(this::toLifestyle).toList())
                .conditions(preferenceConditionRows.stream().map(this::toCondition).toList())
                .conditionWeights(preferenceConditionWeightRows.stream().map(this::toConditionWeight).toList())
                .authentications(authenticationTypes)
                // TODO: 계산식 확정 후 변경
                .compatibility(null)
                .build();
    }

    @Override
    @Transactional
    public MatchDto.Response likeMatching(Long senderId, Long receiverId) {
        Member sender = memberRepository.findById(senderId)
                .orElseThrow(() -> new BusinessException(MemberErrorCode.MEMBER_NOT_FOUND));

        Member receiver = memberRepository.findById(receiverId)
                .orElseThrow(() -> new BusinessException(MemberErrorCode.MEMBER_NOT_FOUND));

        Optional<MemberInterest> optionalMemberInterest =
                memberInterestRepository.findBySenderIdAndReceiverIdForUpdate(senderId, receiverId);

        optionalMemberInterest.ifPresentOrElse(
                MemberInterest::toggle,
                () -> saveMemberInterest(sender, receiver)
        );

        return new MatchDto.Response(LocalDateTime.now());
    }

    public void saveMemberInterest(Member sender, Member receiver) {
        MemberInterest memberInterest = MemberInterest.builder()
                .sender(sender)
                .receiver(receiver)
                .build();
        memberInterestRepository.save(memberInterest);
    }

    @Override
    @Transactional
    public MemberReportDto.Response reportMatching(Long reporterId, Long reportedId, MemberReportDto.Request request) {
        Member reporter = memberRepository.findById(reporterId)
                .orElseThrow(() -> new BusinessException(MemberErrorCode.MEMBER_NOT_FOUND));

        Member reported = memberRepository.findById(reportedId)
                .orElseThrow(() -> new BusinessException(MemberErrorCode.MEMBER_NOT_FOUND));

        boolean exist = memberDeclarationRepository.existsByReporterAndReported(reporter, reported);

        if (exist) {
            throw new BusinessException(MemberErrorCode.DECLARATION_DUPLICATE);
        } else {
            saveMemberDeclaration(reporter, reported, request.getContents());
        }

        return new MemberReportDto.Response(LocalDateTime.now());
    }

    private void saveMemberDeclaration(Member reporter, Member reported, String reason) {
        MemberDeclaration memberDeclaration = MemberDeclaration.builder()
                .reporter(reporter)
                .reported(reported)
                .reason(reason)
                .build();

        memberDeclarationRepository.save(memberDeclaration);
    }

}
