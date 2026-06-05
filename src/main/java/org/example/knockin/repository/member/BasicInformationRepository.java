package org.example.knockin.repository.member;

import org.example.knockin.entity.member.BasicInformation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BasicInformationRepository extends JpaRepository<BasicInformation, Long>, BasicInformationRepositoryCustom {
}