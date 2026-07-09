package org.example.knockin.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.knockin.entity.board.RoommateBoard;
import org.example.knockin.entity.board.RoommateBoardInterest;
import org.example.knockin.entity.member.Member;
import org.example.knockin.repository.board.RoommateBoardInterestRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoommateBoardInterestServiceImpl {
    private final RoommateBoardInterestRepository roommateBoardInterestRepository;

    public boolean existsActiveByBoardIdAndMemberId(Long boardId, Long memberId) {
        return roommateBoardInterestRepository.existsByRoommateBoardIdAndMemberIdAndIsDeletedIsFalse(boardId, memberId);
    }

    public void toggle(Member member, RoommateBoard roommateBoard) {
        roommateBoardInterestRepository.findByRoommateBoardAndMember(roommateBoard, member)
                .ifPresentOrElse(
                        RoommateBoardInterest::likeToggle,
                        () -> save(member, roommateBoard)
                );
    }

    private void save(Member member, RoommateBoard roommateBoard) {
        RoommateBoardInterest roommateBoardInterest = RoommateBoardInterest.builder()
                .member(member)
                .roommateBoard(roommateBoard)
                .isDeleted(false)
                .build();
        roommateBoardInterestRepository.save(roommateBoardInterest);
    }
}
