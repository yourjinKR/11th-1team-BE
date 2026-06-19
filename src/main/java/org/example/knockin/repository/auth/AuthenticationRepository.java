package org.example.knockin.repository.auth;

import org.example.knockin.entity.auth.Authentication;
import org.example.knockin.entity.auth.AuthenticationType;
import org.example.knockin.entity.member.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AuthenticationRepository extends JpaRepository<Authentication, Long>, AuthenticationRepositoryCustom {
    List<Authentication> findByMemberAndIsDeletedAndIsAccepted(Member member, Boolean isDeleted, Boolean isAccepted);

    List<Authentication> findByMemberAndIsDeletedAndIsAcceptedAndType(Member member, Boolean isDeleted, Boolean isAccepted, AuthenticationType type);
    Optional<Authentication> findFirstByMemberAndIsDeletedAndIsAcceptedAndTypeOrderByCreatedAtDesc(Member member, Boolean isDeleted, Boolean isAccepted, AuthenticationType type);
}