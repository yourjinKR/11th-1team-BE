package org.example.knockin.repository.member;

import org.example.knockin.entity.member.Search;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SearchRepository extends JpaRepository<Search, Long> {
}