package org.example.knockin.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import org.example.knockin.dto.BoardDto;
import org.example.knockin.entity.member.Member;
import org.example.knockin.global.api.CommonResponse;
import org.example.knockin.global.auth.dto.PrincipalDetails;
import org.example.knockin.service.RoommateBoardService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
@DisplayName("룸메이트 게시글 컨트롤러")
class RoomMateControllerTest {

    @Mock
    private RoommateBoardService roommateBoardService;

    @Mock
    private PrincipalDetails principalDetails;

    @Mock
    private Member member;

    @InjectMocks
    private RoomMateController roomMateController;

    @Test
    @DisplayName("게시글 저장 요청 시 인증된 회원 ID를 서비스에 전달하고 저장 결과를 반환한다")
    void saveBoardPassesAuthenticatedMemberIdToService() {
        BoardDto.Request request = new BoardDto.Request();
        Long memberId = 42L;
        BoardDto.Response serviceResponse = new BoardDto.Response(
                LocalDateTime.of(2026, 6, 4, 16, 30));
        when(principalDetails.getMember()).thenReturn(member);
        when(member.getId()).thenReturn(memberId);
        when(roommateBoardService.save(request, memberId)).thenReturn(serviceResponse);

        CommonResponse<BoardDto.Response> response = roomMateController.saveBoard(
                request, principalDetails);

        verify(roommateBoardService).save(request, memberId);
        assertThat(response.getStatusValue()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getBody()).isSameAs(serviceResponse);
        assertThat(response.getError()).isNull();
    }
}
