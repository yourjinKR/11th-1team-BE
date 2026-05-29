package org.example.knockin.repository.member;

import java.util.List;
import org.example.knockin.entity.member.Member;

public interface MemberRepositoryCustom {
    List<Member> searchMembers(String providerId, String providerType);
}
