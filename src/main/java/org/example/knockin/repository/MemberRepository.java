package org.example.knockin.repository;

import java.util.Optional;
import org.example.knockin.entity.LoginProviderType;
import org.example.knockin.entity.MemberEntity;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.jpa.repository.JpaRepository;

@NullMarked
public interface MemberRepository extends JpaRepository<MemberEntity, Long>, MemberRepositoryCustom {
    Optional<MemberEntity> findByMemberId(Long memberId);
    Optional<MemberEntity> findByProviderTypeAndProviderId(LoginProviderType providerType, String providerId);
}
