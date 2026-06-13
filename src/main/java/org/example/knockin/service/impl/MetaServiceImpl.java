package org.example.knockin.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.knockin.dto.*;
import org.example.knockin.entity.agreement.Agreement;
import org.example.knockin.entity.agreement.AgreementLog;
import org.example.knockin.entity.life.LifePattern;
import org.example.knockin.entity.life.LifePatternInformation;
import org.example.knockin.entity.room.Region;
import org.example.knockin.entity.room.RoomExtraOption;
import org.example.knockin.entity.room.RoomType;
import org.example.knockin.global.exception.BusinessException;
import org.example.knockin.global.exception.MetaErrorCode;
import org.example.knockin.repository.agreement.AgreementLogRepository;
import org.example.knockin.repository.agreement.AgreementRepository;
import org.example.knockin.repository.life.LifePatternInformationRepository;
import org.example.knockin.repository.life.LifePatternRepository;
import org.example.knockin.repository.member.SearchRepository;
import org.example.knockin.repository.room.RegionRepository;
import org.example.knockin.repository.room.RoomExtraOptionRepository;
import org.example.knockin.repository.room.RoomTypeRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class MetaServiceImpl {
    private final AgreementLogRepository agreementLogRepository;
    private final LifePatternInformationRepository lifePatternInformationRepository;
    private final LifePatternRepository lifePatternRepository;
    private final RegionRepository regionRepository;
    private final RoomTypeRepository roomTypeRepository;
    private final RoomExtraOptionRepository roomExtraOptionRepository;
    private final AgreementRepository agreementRepository;
    private final SearchRepository searchRepository;

    public List<AgreementLog> findByAgreementLogIsCurrent(List<Long> agreementIds) {
        return agreementLogRepository.findByAgreementLogIsCurrent(agreementIds);
    }

    public List<LifePatternInformation> findByLifeStyle(List<Long> lifeStyles) {
        return lifePatternInformationRepository.findByLifeStyles(lifeStyles);
    }

    public List<LifePattern> findLifePatternByLifeStyle(List<Long> lifeStyles) {
        return lifePatternRepository.findAllById(lifeStyles);
    }

    public Optional<Region> findByRegionId(Long id) {
        return regionRepository.findById(id);
    }

    public List<Region> findByRegions(List<Long> regions) {
        return regionRepository.findByRegions(regions);
    }

    public List<Region> findByRegionAndChild(List<Long> regionIds) {
        return regionRepository.findByIdInWithChild(regionIds);
    }

    public Optional<RoomType> findByRoomTypeId(Long roomTypeId) {
        return roomTypeRepository.findById(roomTypeId);
    }

    public List<RoomType> findByRoomTypes(List<Long> roomTypes) {
        return roomTypeRepository.findByRoomTypes(roomTypes);
    }

    public List<RoomExtraOption> findRoomExtraOptionsByIdIn(List<Long> ids) {
        return roomExtraOptionRepository.findByIdIn(ids);
    }

    public TermsListDto.Response findTermList() {
        List<TermsListDto.Response.TermsItem> termsItemList = agreementRepository.findAllByIsDeleted(false).stream().map(item ->
            TermsListDto.Response.TermsItem.builder().id(item.getId()).title(item.getTitle()).build()).toList();
        return TermsListDto.Response.builder().terms(termsItemList).build();
    }

    public TermsDetailDto.Response findTermDetail(Long termsId) {
        Agreement agreement = agreementRepository.findById(termsId).orElseThrow(() -> new BusinessException(MetaErrorCode.TERM_NOT_FOUND));
        return TermsDetailDto.Response.builder().id(agreement.getId()).contents(agreement.getContents()).build();
    }

    public PopularSearchDto.Response findPopSearch() {
        List<PopularSearchDto.Response.RankItem> rankItems = searchRepository.findPopSearch();
        IntStream.range(0, rankItems.size()).forEach(i -> rankItems.get(i).setId(i + 1L));
        return PopularSearchDto.Response.builder().rank(rankItems).build();
    }

    public MetaLifestylePatternsDto.Response findLifeStylePatterns() {
        return MetaLifestylePatternsDto.Response.builder().patterns(lifePatternRepository.findLifeStylePatterns()).build();
    }

    public MetaRoomTypesDto.Response findRoomTypes() {
        return MetaRoomTypesDto.Response.builder()
                .roomType(roomTypeRepository.findAllByIsDeleted(false).stream().map(item ->
                        MetaRoomTypesDto.Response.RoomTypeItem.builder().id(item.getId()).name(item.getName()).build()).toList())
                .build();
    }

    public MetaRegionsDto.Response findRegions() {
        return MetaRegionsDto.Response.builder()
                .region(regionRepository.findAll().stream().map(item ->
                        MetaRegionsDto.Response.RegionItem.builder().id(item.getId()).name(item.getName()).parentId(item.getParent().getId()).build()).toList())
                .build();
    }

    public MetaRoomAddOptionsDto.Response findRoomAddOptions() {
        return MetaRoomAddOptionsDto.Response.builder()
                .roomAddOption(roomExtraOptionRepository.findAllByIsDeleted(false).stream().map(item ->
                        MetaRoomAddOptionsDto.Response.RoomAddOptionItem.builder().id(item.getId()).name(item.getName()).build()).toList())
                .build();
    }
}
