package org.example.knockin.repository.member;

import org.example.knockin.entity.member.Member;
import org.example.knockin.entity.member.MemberPrivacy;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.SequencedCollection;

public interface MemberPrivacyRepository extends JpaRepository<MemberPrivacy, Long> {
    List<MemberPrivacy> findByMember(Member member);

    List<MemberPrivacy> findByMemberId(Long memberId);
}