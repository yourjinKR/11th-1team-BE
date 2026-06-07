package org.example.knockin.repository.room;

import org.example.knockin.entity.room.OfferRoomType;
import org.example.knockin.entity.room.RoomOfferProfile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OfferRoomTypeRepository extends JpaRepository<OfferRoomType, Long> {
    void deleteByRoomOfferProfile(RoomOfferProfile roomOfferProfile);
}