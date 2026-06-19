package org.example.knockin.entity.auth;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.*;
import org.example.knockin.entity.member.Member;
import org.example.knockin.global.jpa.CreatedAtEntity;
import org.hibernate.annotations.ColumnDefault;


@Getter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "authentication")
public class Authentication extends CreatedAtEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private AuthenticationType type;

    @Column(name = "email", nullable = false, length = 50)
    private String email;

    @Column(name = "code", nullable = false, length = 100)
    private String code;

    @ColumnDefault("false")
    @Column(name = "is_accepted", nullable = false)
    private Boolean isAccepted;

    @ColumnDefault("false")
    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted;

    public void deleteAuthentication() {
        this.isDeleted = true;
    }

    public void acceptAuthentication() {
        this.isAccepted = true;
    }
}
