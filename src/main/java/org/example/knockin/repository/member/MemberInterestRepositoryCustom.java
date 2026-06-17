package org.example.knockin.repository.member;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.example.knockin.entity.member.MemberInterest;

public interface MemberInterestRepositoryCustom {
    List<Long> findActiveReceiverIdsBySenderIdAndReceiverIds(Long senderId, Collection<Long> receiverIds);

    Optional<MemberInterest> findBySenderIdAndReceiverIdForUpdate(Long senderId, Long receiverId);
}
