package org.example.knockin.repository.member;

import org.example.knockin.entity.member.BasicInformation;
import org.example.knockin.entity.member.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BasicInformationRepository extends JpaRepository<BasicInformation, Long>, BasicInformationRepositoryCustom {
    List<BasicInformation> findByMember(Member member);
}