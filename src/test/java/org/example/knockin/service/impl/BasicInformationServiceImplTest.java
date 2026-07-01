package org.example.knockin.service.impl;
 
import org.example.knockin.entity.member.BasicInformation;
import org.example.knockin.entity.member.Member;
import org.example.knockin.repository.member.BasicInformationRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
 
import java.util.List;
 
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
 
@ExtendWith(MockitoExtension.class)
class BasicInformationServiceImplTest {
 
    @Mock
    private BasicInformationRepository basicInformationRepository;
 
    @InjectMocks
    private BasicInformationServiceImpl basicInformationService;
 
    @Test
    @DisplayName("회원으로 기본 정보 찾기 테스트")
    void findByMemberTest() {
        Member member = mock(Member.class);
        BasicInformation info = mock(BasicInformation.class);
        given(basicInformationRepository.findByMember(member)).willReturn(List.of(info));
 
        List<BasicInformation> result = basicInformationService.findByMember(member);
 
        assertThat(result).hasSize(1);
        verify(basicInformationRepository).findByMember(member);
    }
 
    @Test
    @DisplayName("기본 정보 저장 테스트")
    void saveTest() {
        BasicInformation info = mock(BasicInformation.class);
        given(basicInformationRepository.save(any(BasicInformation.class))).willReturn(info);
 
        BasicInformation result = basicInformationService.save(info);
 
        assertThat(result).isEqualTo(info);
        verify(basicInformationRepository).save(info);
    }
}
