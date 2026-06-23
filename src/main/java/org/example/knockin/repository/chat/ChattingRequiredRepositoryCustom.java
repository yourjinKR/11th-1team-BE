package org.example.knockin.repository.chat;

import java.util.Optional;
import org.example.knockin.entity.chat.ChattingRequired;
import org.example.knockin.entity.member.Member;

public interface ChattingRequiredRepositoryCustom {
    boolean existsBetweenMembers(Member memberA, Member memberB);

    Optional<ChattingRequired> findLatest(Member memberA, Member memberB);
}
