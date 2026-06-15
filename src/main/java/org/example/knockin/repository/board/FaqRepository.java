package org.example.knockin.repository.board;

import org.example.knockin.entity.board.Faq;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FaqRepository extends JpaRepository<Faq,Long> {
}
