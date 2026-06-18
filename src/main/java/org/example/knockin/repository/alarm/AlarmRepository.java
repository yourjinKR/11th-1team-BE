package org.example.knockin.repository.alarm;

import org.example.knockin.entity.alarm.Alarm;
import org.example.knockin.entity.member.Member;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public interface AlarmRepository extends JpaRepository<Alarm, Long> {
    List<Alarm> findByMember(Member member, Pageable pageable);

    Optional<Alarm> findByIdAndMember(Long id, Member member);

    List<Alarm> findByMemberAndIsRead(Member member, Boolean isRead);
}