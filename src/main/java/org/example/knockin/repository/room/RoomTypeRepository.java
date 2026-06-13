package org.example.knockin.repository.room;

import org.example.knockin.entity.room.RoomType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoomTypeRepository extends JpaRepository<RoomType, Long>, RoomTypeRepositoryCustom {
    List<RoomType> findAllByIsDeleted(Boolean isDeleted);
}