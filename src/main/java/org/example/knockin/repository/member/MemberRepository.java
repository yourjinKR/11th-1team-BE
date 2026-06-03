package org.example.knockin.repository.member;

import java.util.Optional;
import org.example.knockin.entity.auth.LoginProviderType;
import org.example.knockin.entity.member.Member;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.jpa.repository.JpaRepository;

@NullMarked
public interface MemberRepository extends JpaRepository<Member, Long>, MemberRepositoryCustom {
    Optional<Member> findById(Long memberId);
}
