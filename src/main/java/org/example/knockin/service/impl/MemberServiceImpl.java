package org.example.knockin.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.knockin.entity.MemberEntity;
import org.example.knockin.repository.MemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl {
    private final MemberRepository memberRepository;

    @Transactional
    public MemberEntity save(String name) {
        return memberRepository.save(MemberEntity.builder().name(name).build());
    }

    public List<MemberEntity> list() {
        String searchName = null;
        return memberRepository.searchMembers(searchName);
    }
}
