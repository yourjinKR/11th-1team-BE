package org.example.knockin.service.impl;


import lombok.RequiredArgsConstructor;
import org.example.knockin.entity.room.RoomType;
import org.example.knockin.global.exception.BusinessException;
import org.example.knockin.global.exception.RoomTypeErrorCode;
import org.example.knockin.repository.room.RoomTypeRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoomTypeServiceImpl {
    private final RoomTypeRepository roomTypeRepository;

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
}
