package org.example.knockin.service.impl;
 
import org.example.knockin.entity.life.MemberLifePattern;
import org.example.knockin.entity.life.MemberLifePatternLog;
import org.example.knockin.entity.member.Member;
import org.example.knockin.repository.life.MemberLifePatternLogRepository;
import org.example.knockin.repository.life.MemberLifePatternRepository;
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
class MemberLifePatternServiceTest {
 
    @Mock
    private MemberLifePatternRepository memberLifePatternRepository;
 
    @Mock
    private MemberLifePatternLogRepository memberLifePatternLogRepository;
 
    @InjectMocks
    private MemberLifePatternService memberLifePatternService;
 
    @Test
    @DisplayName("회원 라이프스타일 로그 모두 저장 테스트")
    void saveMemberLifePatternLogAllTest() {
        MemberLifePatternLog log = mock(MemberLifePatternLog.class);
        List<MemberLifePatternLog> list = List.of(log);
        given(memberLifePatternLogRepository.saveAll(list)).willReturn(list);
 
        List<MemberLifePatternLog> result = memberLifePatternService.saveMemberLifePatternLogAll(list);
 
        assertThat(result).hasSize(1);
        verify(memberLifePatternLogRepository).saveAll(list);
    }
 
    @Test
    @DisplayName("회원 라이프스타일 패턴 모두 저장 테스트")
    void saveMemberLifePatternAllTest() {
        MemberLifePattern pattern = mock(MemberLifePattern.class);
        List<MemberLifePattern> list = List.of(pattern);
        given(memberLifePatternRepository.saveAll(list)).willReturn(list);
 
        List<MemberLifePattern> result = memberLifePatternService.saveMemberLifePatternAll(list);
 
        assertThat(result).hasSize(1);
        verify(memberLifePatternRepository).saveAll(list);
    }
 
    @Test
    @DisplayName("회원별 라이프스타일 패턴 조회 테스트")
    void findByMemberTest() {
        Member member = mock(Member.class);
        MemberLifePattern pattern = mock(MemberLifePattern.class);
        given(memberLifePatternRepository.findByMember(member)).willReturn(List.of(pattern));
 
        List<MemberLifePattern> result = memberLifePatternService.findByMember(member);
 
        assertThat(result).hasSize(1);
        verify(memberLifePatternRepository).findByMember(member);
    }
}
