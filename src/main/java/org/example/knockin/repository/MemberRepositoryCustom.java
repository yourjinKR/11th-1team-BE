package org.example.knockin.repository;

import org.example.knockin.entity.MemberEntity;

import java.util.List;

public interface MemberRepositoryCustom {
    List<MemberEntity> searchMembers(String username);
}
