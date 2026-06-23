package org.example.knockin.repository.room;

import org.example.knockin.entity.room.RoommateMatchingRequired;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoommateMatchingRequiredRepository extends JpaRepository<RoommateMatchingRequired, Long>, RoommateMatchingRequiredRepositoryCustom {
}