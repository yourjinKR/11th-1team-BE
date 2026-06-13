package org.example.knockin.repository.room;

import java.util.Collection;
import java.util.List;
import org.example.knockin.entity.room.RoomExtraOption;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoomExtraOptionRepository extends JpaRepository<RoomExtraOption, Long> {
    List<RoomExtraOption> findByIdIn(Collection<Long> ids);

    List<RoomExtraOption> findAllByIsDeleted(Boolean isDeleted);
}