package org.example.knockin.repository.alarm;

import org.example.knockin.entity.alarm.AlarmSetting;
import org.example.knockin.entity.member.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AlarmSettingRepository extends JpaRepository<AlarmSetting, Long> {
    List<AlarmSetting> findByMember(Member member);

    AlarmSetting findByIdAndMember(Long id, Member member);
}