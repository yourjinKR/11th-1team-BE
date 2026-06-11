package org.example.knockin.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.knockin.entity.agreement.AgreementLog;
import org.example.knockin.entity.life.LifePattern;
import org.example.knockin.entity.life.LifePatternInformation;
import org.example.knockin.entity.room.Region;
import org.example.knockin.entity.room.RoomExtraOption;
import org.example.knockin.entity.room.RoomType;
import org.example.knockin.repository.agreement.AgreementLogRepository;
import org.example.knockin.repository.life.LifePatternInformationRepository;
import org.example.knockin.repository.life.LifePatternRepository;
import org.example.knockin.repository.room.RegionRepository;
import org.example.knockin.repository.room.RoomExtraOptionRepository;
import org.example.knockin.repository.room.RoomTypeRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MetaServiceImpl {
    private final AgreementLogRepository agreementLogRepository;
    private final LifePatternInformationRepository lifePatternInformationRepository;
    private final LifePatternRepository lifePatternRepository;
    private final RegionRepository regionRepository;
    private final RoomTypeRepository roomTypeRepository;
    private final RoomExtraOptionRepository roomExtraOptionRepository;

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
}
