package org.example.knockin.repository.life;

import org.example.knockin.entity.life.PreferenceCondition;
import org.example.knockin.entity.member.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PreferenceConditionRepository extends JpaRepository<PreferenceCondition, Long> {
    List<PreferenceCondition> findByMember(Member member);
}