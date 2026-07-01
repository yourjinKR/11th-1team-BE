package org.example.knockin.service.impl;
 
import org.example.knockin.entity.life.PreferenceCondition;
import org.example.knockin.entity.life.PreferenceConditionLog;
import org.example.knockin.entity.life.PreferenceConditionWeight;
import org.example.knockin.entity.life.PreferenceConditionWeightLog;
import org.example.knockin.entity.member.Member;
import org.example.knockin.repository.life.PreferenceConditionLogRepository;
import org.example.knockin.repository.life.PreferenceConditionRepository;
import org.example.knockin.repository.life.PreferenceConditionWeightLogRepository;
import org.example.knockin.repository.life.PreferenceConditionWeightRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
 
import java.util.List;
 
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
 
@ExtendWith(MockitoExtension.class)
class PreferenceConditionServiceImplTest {
 
    @Mock private PreferenceConditionRepository preferenceConditionRepository;
    @Mock private PreferenceConditionLogRepository preferenceConditionLogRepository;
    @Mock private PreferenceConditionWeightRepository preferenceConditionWeightRepository;
    @Mock private PreferenceConditionWeightLogRepository preferenceConditionWeightLogRepository;
 
    @InjectMocks
    private PreferenceConditionServiceImpl preferenceConditionService;
 
    @Test
    @DisplayName("선호도 조건 목록 저장 테스트")
    void preferenceConditionSaveAllTest() {
        PreferenceCondition pc = mock(PreferenceCondition.class);
        List<PreferenceCondition> list = List.of(pc);
        given(preferenceConditionRepository.saveAll(list)).willReturn(list);
 
        List<PreferenceCondition> result = preferenceConditionService.preferenceConditionSaveAll(list);
 
        assertThat(result).hasSize(1);
        verify(preferenceConditionRepository).saveAll(list);
    }
 
    @Test
    @DisplayName("선호도 조건 로그 목록 저장 테스트")
    void preferenceConditionLogSaveAllTest() {
        PreferenceConditionLog log = mock(PreferenceConditionLog.class);
        List<PreferenceConditionLog> list = List.of(log);
        given(preferenceConditionLogRepository.saveAll(list)).willReturn(list);
 
        List<PreferenceConditionLog> result = preferenceConditionService.preferenceConditionLogSaveAll(list);
 
        assertThat(result).hasSize(1);
        verify(preferenceConditionLogRepository).saveAll(list);
    }
 
    @Test
    @DisplayName("선호도 가중치 목록 저장 테스트")
    void preferenceConditionWeightSaveAllTest() {
        PreferenceConditionWeight pcw = mock(PreferenceConditionWeight.class);
        List<PreferenceConditionWeight> list = List.of(pcw);
        given(preferenceConditionWeightRepository.saveAll(list)).willReturn(list);
 
        List<PreferenceConditionWeight> result = preferenceConditionService.preferenceConditionWeightSaveAll(list);
 
        assertThat(result).hasSize(1);
        verify(preferenceConditionWeightRepository).saveAll(list);
    }
 
    @Test
    @DisplayName("선호도 가중치 로그 목록 저장 테스트")
    void preferenceConditionWeightLogSaveAllTest() {
        PreferenceConditionWeightLog log = mock(PreferenceConditionWeightLog.class);
        List<PreferenceConditionWeightLog> list = List.of(log);
        given(preferenceConditionWeightLogRepository.saveAll(list)).willReturn(list);
 
        List<PreferenceConditionWeightLog> result = preferenceConditionService.preferenceConditionWeightLogSaveAll(list);
 
        assertThat(result).hasSize(1);
        verify(preferenceConditionWeightLogRepository).saveAll(list);
    }
 
    @Test
    @DisplayName("회원별 선호도 조건 목록 조회 테스트")
    void findPreferenceConditionByMemberTest() {
        Member member = mock(Member.class);
        PreferenceCondition pc = mock(PreferenceCondition.class);
        given(preferenceConditionRepository.findByMember(member)).willReturn(List.of(pc));
 
        List<PreferenceCondition> result = preferenceConditionService.findPreferenceConditionByMember(member);
 
        assertThat(result).hasSize(1);
        verify(preferenceConditionRepository).findByMember(member);
    }
 
    @Test
    @DisplayName("회원별 선호도 가중치 목록 조회 테스트")
    void findPreferenceConditionWeightByMemberTest() {
        Member member = mock(Member.class);
        PreferenceConditionWeight pcw = mock(PreferenceConditionWeight.class);
        given(preferenceConditionWeightRepository.findByMember(member)).willReturn(List.of(pcw));
 
        List<PreferenceConditionWeight> result = preferenceConditionService.findPreferenceConditionWeightByMember(member);
 
        assertThat(result).hasSize(1);
        verify(preferenceConditionWeightRepository).findByMember(member);
    }
 
    @Test
    @DisplayName("회원별 선호도 가중치 삭제 테스트")
    void deletePreferenceConditionWeightByMemberTest() {
        Member member = mock(Member.class);
 
        preferenceConditionService.deletePreferenceConditionWeightByMember(member);
 
        verify(preferenceConditionWeightRepository).deleteByMember(member);
        verify(preferenceConditionWeightRepository).flush();
    }
}
