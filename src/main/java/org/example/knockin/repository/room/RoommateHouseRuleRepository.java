package org.example.knockin.repository.room;

import java.util.List;
import org.example.knockin.entity.room.RoommateHouseRule;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoommateHouseRuleRepository extends JpaRepository<RoommateHouseRule, Long>, RoommateHouseRuleRepositoryCustom {
    List<RoommateHouseRule> findByMyRoommateIdAndIsDeleted(Long myRoommateId, Boolean isDeleted);
}
