package org.example.knockin.repository.room;

import java.util.Optional;
import org.example.knockin.entity.room.RoommateHouseRule;

public interface RoommateHouseRuleRepositoryCustom {
    Optional<RoommateHouseRule> findWithFetchedById(Long id);
}
