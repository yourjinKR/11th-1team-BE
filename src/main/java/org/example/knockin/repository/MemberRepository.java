package org.example.knockin.repository;

import lombok.NonNull;
import org.example.knockin.entity.MemberEntity;
import org.example.knockin.entity.member.LoginProvider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<@NonNull MemberEntity, @NonNull Long>, MemberRepositoryCustom {
    Optional<MemberEntity> findByProviderAndProviderId(LoginProvider provider, String providerId);
}
