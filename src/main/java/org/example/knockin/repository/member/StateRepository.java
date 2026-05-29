package org.example.knockin.repository.member;

import org.example.knockin.entity.member.State;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StateRepository extends JpaRepository<State, Long> {
}