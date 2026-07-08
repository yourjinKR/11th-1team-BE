package org.example.knockin.service.impl;


import lombok.RequiredArgsConstructor;
import org.example.knockin.entity.room.*;
import org.example.knockin.exception.BusinessException;
import org.example.knockin.exception.RoomTypeErrorCode;
import org.example.knockin.repository.room.OfferRoomTypeRepository;
import org.example.knockin.repository.room.RoomTypeRepository;
import org.example.knockin.repository.room.SeekerRoomTypeRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoomTypeServiceImpl {
    private final RoomTypeRepository roomTypeRepository;
    private final OfferRoomTypeRepository offerRoomTypeRepository;
    private final SeekerRoomTypeRepository seekerRoomTypeRepository;

    @Transactional
    public RoomType saveRoomType(RoomType roomType) {
        return roomTypeRepository.save(roomType);
    }

    @Transactional
    public RoomType modifyRoomType(RoomType roomType, Long roomTypeId) {
        RoomType roomTypeEntity = roomTypeRepository.findById(roomTypeId).orElseThrow(() -> new BusinessException(RoomTypeErrorCode.ROOM_TYPE_NOT_FOUNT));
        roomTypeEntity.modifyRoomType(roomType);
        return roomTypeEntity;
    }

    @Transactional
    public RoomType deleteRoomType(Long roomTypeId) {
        RoomType roomTypeEntity = roomTypeRepository.findById(roomTypeId).orElseThrow(() -> new BusinessException(RoomTypeErrorCode.ROOM_TYPE_NOT_FOUNT));
        roomTypeEntity.deleteRoomType();
        return roomTypeEntity;
    }

    public List<RoomType> findRoomTypeList(Pageable pageable) {
        return roomTypeRepository.findAll(pageable).stream().toList();
    }

    public RoomType findRoomType(Long roomTypeId) {
        return roomTypeRepository.findById(roomTypeId).orElseThrow(() -> new BusinessException(RoomTypeErrorCode.ROOM_TYPE_NOT_FOUNT));
    }

    public List<RoomType> findByRoomTypes(List<Long> roomTypes) {
        return roomTypeRepository.findByRoomTypes(roomTypes);
    }

    public List<RoomType> findAllByIsDeleted(boolean isDeleted) {
        return roomTypeRepository.findAllByIsDeleted(isDeleted);
    }

    @Transactional
    public List<OfferRoomType> saveOfferRoomTypeAll(List<OfferRoomType> offerRoomTypeList) {
        return offerRoomTypeRepository.saveAll(offerRoomTypeList);
    }

    @Transactional
    public List<SeekerRoomType> saveSeekerRoomTypeAll(List<SeekerRoomType> seekerRoomTypes) {
        return seekerRoomTypeRepository.saveAll(seekerRoomTypes);
    }

    @Transactional
    public RoomOfferProfile deleteByRoomOfferProfile(RoomOfferProfile roomOfferProfile) {
        offerRoomTypeRepository.deleteByRoomOfferProfile(roomOfferProfile);
        return roomOfferProfile;
    }

    @Transactional
    public RoomSeekerProfile deleteByRoomSeekerProfile(RoomSeekerProfile seekerProfile) {
        seekerRoomTypeRepository.deleteByRoomSeekerProfile(seekerProfile);
        return seekerProfile;
    }
}
