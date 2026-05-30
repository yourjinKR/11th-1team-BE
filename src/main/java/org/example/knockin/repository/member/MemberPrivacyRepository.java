package org.example.knockin.repository.member;

import org.example.knockin.entity.member.MemberPrivacy;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberPrivacyRepository extends JpaRepository<MemberPrivacy, Long> {
}