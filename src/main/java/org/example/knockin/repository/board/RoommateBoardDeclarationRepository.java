package org.example.knockin.repository.board;

import java.util.List;
import java.util.Optional;
import org.example.knockin.entity.board.RoommateBoard;
import org.example.knockin.entity.board.RoommateBoardDeclaration;
import org.example.knockin.entity.member.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoommateBoardDeclarationRepository extends JpaRepository<RoommateBoardDeclaration, Long>, RoommateBoardDeclarationRepositoryCustom {
    Optional<RoommateBoardDeclaration> findByRoommateBoardAndMember(RoommateBoard roommateBoard, Member member);
    Optional<RoommateBoardDeclaration> findById(Long id);
}