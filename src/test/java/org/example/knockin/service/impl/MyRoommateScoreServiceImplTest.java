package org.example.knockin.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.example.knockin.entity.room.RoommateScore;
import org.example.knockin.repository.room.RoommateScoreRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("내 룸메이트 점수 서비스")
class MyRoommateScoreServiceImplTest {

    @Mock
    private RoommateScoreRepository roommateScoreRepository;

    @InjectMocks
    private MyRoommateScoreServiceImpl myRoommateScoreService;

    @Test
    @DisplayName("내 룸메이트 ID로 점수 상세 목록을 조회한다")
    void findByRoommateIdReturnsScoreDetails() {
        // Given
        Long myRoommateId = 10L;
        RoommateScore roommateScore = RoommateScore.builder().score(80).build();
        when(roommateScoreRepository.findWithScoreDetailsByMyRoommateId(myRoommateId))
                .thenReturn(List.of(roommateScore));

        // When
        List<RoommateScore> result = myRoommateScoreService.findByRoommateId(myRoommateId);

        // Then
        assertThat(result).containsExactly(roommateScore);
        verify(roommateScoreRepository).findWithScoreDetailsByMyRoommateId(myRoommateId);
    }
}
