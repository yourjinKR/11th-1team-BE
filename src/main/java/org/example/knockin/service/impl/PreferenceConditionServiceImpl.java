package org.example.knockin.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.knockin.entity.life.PreferenceCondition;
import org.example.knockin.entity.life.PreferenceConditionLog;
import org.example.knockin.entity.life.PreferenceConditionWeight;
import org.example.knockin.entity.life.PreferenceConditionWeightLog;
import org.example.knockin.entity.member.Member;
import org.example.knockin.repository.life.PreferenceConditionLogRepository;
import org.example.knockin.repository.life.PreferenceConditionRepository;
import org.example.knockin.repository.life.PreferenceConditionWeightLogRepository;
import org.example.knockin.repository.life.PreferenceConditionWeightRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PreferenceConditionServiceImpl {
    private final PreferenceConditionRepository preferenceConditionRepository;
    private final PreferenceConditionLogRepository preferenceConditionLogRepository;
    private final PreferenceConditionWeightRepository preferenceConditionWeightRepository;
    private final PreferenceConditionWeightLogRepository preferenceConditionWeightLogRepository;

    @Transactional
    public List<PreferenceCondition> preferenceConditionSaveAll(List<PreferenceCondition> preferenceConditionList) {
        return preferenceConditionRepository.saveAll(preferenceConditionList);
    }

    @Transactional
    public List<PreferenceConditionLog> preferenceConditionLogSaveAll(List<PreferenceConditionLog> preferenceConditionLogList) {
        return preferenceConditionLogRepository.saveAll(preferenceConditionLogList);
    }

    @Transactional
    public List<PreferenceConditionWeight> preferenceConditionWeightSaveAll(List<PreferenceConditionWeight> preferenceConditionWeightList) {
        return preferenceConditionWeightRepository.saveAll(preferenceConditionWeightList);
    }

    @Transactional
    public List<PreferenceConditionWeightLog> preferenceConditionWeightLogSaveAll(List<PreferenceConditionWeightLog> preferenceConditionWeightLogList) {
        return preferenceConditionWeightLogRepository.saveAll(preferenceConditionWeightLogList);
    }

    public List<PreferenceCondition> findPreferenceConditionByMember(Member member) {
        return preferenceConditionRepository.findByMember(member);
    }

    public List<PreferenceConditionWeight> findPreferenceConditionWeightByMember(Member member) {
        return preferenceConditionWeightRepository.findByMember(member);
    }

    @Transactional
    public void deletePreferenceConditionWeightByMember(Member member) {
        preferenceConditionWeightRepository.deleteByMember(member);
        preferenceConditionWeightRepository.flush();
    }
}
