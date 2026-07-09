package org.example.knockin.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.knockin.entity.board.RoommateBoard;
import org.example.knockin.entity.board.RoommateBoardDeclaration;
import org.example.knockin.entity.member.Member;
import org.example.knockin.exception.BusinessException;
import org.example.knockin.exception.RoommateBoardErrorCode;
import org.example.knockin.global.entity.DeclarationType;
import org.example.knockin.repository.board.RoommateBoardDeclarationRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoommateBoardDeclarationServiceImpl {
    private final RoommateBoardDeclarationRepository roommateBoardDeclarationRepository;

    public void report(RoommateBoard roommateBoard, Member member, String reason) {
        roommateBoardDeclarationRepository.findByRoommateBoardAndMember(roommateBoard, member)
                .ifPresent(declaration -> {
                    throw new BusinessException(RoommateBoardErrorCode.ROOMMATE_BOARD_DECLARATION_DUPLICATE);
                });

        RoommateBoardDeclaration roommateBoardDeclaration = RoommateBoardDeclaration.builder()
                .member(member)
                .roommateBoard(roommateBoard)
                .reason(reason)
                .declarationType(DeclarationType.PENDING)
                .build();

        roommateBoardDeclarationRepository.save(roommateBoardDeclaration);
    }
}
