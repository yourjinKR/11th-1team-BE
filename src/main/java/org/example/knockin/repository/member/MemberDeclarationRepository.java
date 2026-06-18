package org.example.knockin.repository.member;

import org.example.knockin.entity.member.Member;
import org.example.knockin.entity.member.MemberDeclaration;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberDeclarationRepository extends JpaRepository<MemberDeclaration, Long> {
    boolean existsByReporterAndReported(Member reporter, Member reported);
}
