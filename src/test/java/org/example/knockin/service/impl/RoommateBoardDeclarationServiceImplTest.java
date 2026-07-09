package org.example.knockin.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.example.knockin.entity.board.RoommateBoard;
import org.example.knockin.entity.board.RoommateBoardDeclaration;
import org.example.knockin.entity.member.Member;
import org.example.knockin.exception.BusinessException;
import org.example.knockin.exception.RoommateBoardErrorCode;
import org.example.knockin.global.entity.DeclarationType;
import org.example.knockin.repository.board.RoommateBoardDeclarationRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("룸메이트 게시글 신고 서비스")
class RoommateBoardDeclarationServiceImplTest {

    @Mock
    private RoommateBoardDeclarationRepository roommateBoardDeclarationRepository;

    @InjectMocks
    private RoommateBoardDeclarationServiceImpl roommateBoardDeclarationService;

    @Test
    @DisplayName("신고 이력이 없으면 게시글과 회원 및 사유를 대기 상태 신고로 저장한다")
    void reportSavesPendingDeclarationWhenHistoryDoesNotExist() {
        // Given
        RoommateBoard board = RoommateBoard.builder().id(10L).build();
        Member member = Member.builder().id(1L).build();
        when(roommateBoardDeclarationRepository.findByRoommateBoardAndMember(board, member))
                .thenReturn(Optional.empty());
        ArgumentCaptor<RoommateBoardDeclaration> declarationCaptor =
                ArgumentCaptor.forClass(RoommateBoardDeclaration.class);

        // When
        roommateBoardDeclarationService.report(board, member, "부적절한 내용");

        // Then
        verify(roommateBoardDeclarationRepository).save(declarationCaptor.capture());
        RoommateBoardDeclaration savedDeclaration = declarationCaptor.getValue();
        assertThat(savedDeclaration.getRoommateBoard()).isSameAs(board);
        assertThat(savedDeclaration.getMember()).isSameAs(member);
        assertThat(savedDeclaration.getReason()).isEqualTo("부적절한 내용");
        assertThat(savedDeclaration.getDeclarationType()).isEqualTo(DeclarationType.PENDING);
    }

    @Test
    @DisplayName("이미 신고한 게시글이면 중복 신고 예외를 던지고 새 신고를 저장하지 않는다")
    void reportThrowsWhenDeclarationAlreadyExists() {
        // Given
        RoommateBoard board = RoommateBoard.builder().id(10L).build();
        Member member = Member.builder().id(1L).build();
        RoommateBoardDeclaration existingDeclaration = RoommateBoardDeclaration.builder()
                .roommateBoard(board)
                .member(member)
                .declarationType(DeclarationType.PENDING)
                .build();
        when(roommateBoardDeclarationRepository.findByRoommateBoardAndMember(board, member))
                .thenReturn(Optional.of(existingDeclaration));

        // When & Then
        assertThatThrownBy(() -> roommateBoardDeclarationService.report(board, member, "부적절한 내용"))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getErrorCode())
                                .isEqualTo(RoommateBoardErrorCode.ROOMMATE_BOARD_DECLARATION_DUPLICATE));
        verify(roommateBoardDeclarationRepository, never()).save(any(RoommateBoardDeclaration.class));
    }
}
