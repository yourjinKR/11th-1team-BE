package org.example.knockin.repository.board;

import java.util.List;
import java.util.Optional;
import org.example.knockin.entity.board.RoommateBoard;
import org.example.knockin.entity.board.RoommateBoardInterest;
import org.example.knockin.entity.member.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoommateBoardInterestRepository extends JpaRepository<RoommateBoardInterest, Long> {
    Optional<RoommateBoardInterest> findByRoommateBoardAndMember(RoommateBoard roommateBoard, Member member);

    List<RoommateBoardInterest> findAllByRoommateBoardIdAndMemberId(Long roommateBoardId, Long memberId);

    boolean existsByRoommateBoardIdAndMemberIdAndIsDeletedIsFalse(Long roommateBoardId, Long memberId);
}
