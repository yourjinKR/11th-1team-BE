package org.example.knockin.service.impl;

import org.example.knockin.entity.member.Member;
import org.example.knockin.repository.room.MyRoommateRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class MyRoomMateServiceImplTest {

    @Mock
    private MyRoommateRepository myRoommateRepository;

    @InjectMocks
    private MyRoomMateServiceImpl myRoomMateService;

    @Test
    @DisplayName("룸메이트 존재 여부 확인 테스트")
    void isExistRoomMateTest() {
        // given
        Member member = mock(Member.class);
        given(myRoommateRepository.isExistRoomMate(member)).willReturn(true);

        // when
        boolean result = myRoomMateService.isExistRoomMate(member);

        // then
        assertThat(result).isTrue();
    }
}
