package org.example.knockin.repository.member;

import org.example.knockin.entity.member.Member;
import org.example.knockin.entity.member.State;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StateRepository extends JpaRepository<State, Long> {
    List<State> findByMember(Member member);
}