package org.example.knockin.repository.member;

import org.example.knockin.entity.member.MemberInterest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberInterestRepository extends JpaRepository<MemberInterest, Long>, MemberInterestRepositoryCustom {
    boolean existsBySenderIdAndReceiverId(Long senderId, Long receiverId);
}
