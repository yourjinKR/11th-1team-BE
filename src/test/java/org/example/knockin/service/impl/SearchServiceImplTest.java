package org.example.knockin.service.impl;
 
import org.example.knockin.dto.PopularSearchDto;
import org.example.knockin.repository.member.SearchRepository;
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
class SearchServiceImplTest {
 
    @Mock
    private SearchRepository searchRepository;
 
    @InjectMocks
    private SearchServiceImpl searchService;
 
    @Test
    @DisplayName("인기 검색어 조회 테스트")
    void findPopSearchTest() {
        PopularSearchDto.Response.RankItem rankItem = PopularSearchDto.Response.RankItem.builder().keyword("검색어").build();
        given(searchRepository.findPopSearch()).willReturn(List.of(rankItem));
 
        List<PopularSearchDto.Response.RankItem> result = searchService.findPopSearch();
 
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getKeyword()).isEqualTo("검색어");
        verify(searchRepository).findPopSearch();
    }
}
