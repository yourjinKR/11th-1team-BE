package org.example.knockin.repository.chat;

import org.example.knockin.entity.member.Member;

public interface ChattingRequiredRepositoryCustom {
    boolean existsBetweenMembers(Member memberA, Member memberB);
}
