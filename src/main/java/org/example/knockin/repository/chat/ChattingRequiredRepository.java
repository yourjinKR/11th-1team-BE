package org.example.knockin.repository.chat;

import org.example.knockin.entity.chat.ChattingRequired;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChattingRequiredRepository extends JpaRepository<ChattingRequired, Long> {
}