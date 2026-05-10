package org.example.knockin.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.knockin.entity.member.LoginProvider;
import org.example.knockin.entity.member.MemberRole;
import org.example.knockin.entity.member.MemberStatus;

@Entity
@Table(
        name = "member",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_member_provider_provider_id",
                        columnNames = {"provider", "provider_id"}
                )
        }
)
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MemberEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long memberId;

    private String name;

    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider", nullable = false, length = 20)
    private LoginProvider provider;

    @Column(name = "provider_id", nullable = false)
    private String providerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MemberRole role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MemberStatus status;

    public static MemberEntity pendingMember(LoginProvider provider, String providerId, String email) {
        return MemberEntity.builder()
                .provider(provider)
                .providerId(providerId)
                .email(email)
                .status(MemberStatus.PENDING)
                .role(MemberRole.USER)
                .build();
    }

    public void completeOnboarding(String name) {
        this.name = name;
        this.status = MemberStatus.ACTIVE;
    }
}
