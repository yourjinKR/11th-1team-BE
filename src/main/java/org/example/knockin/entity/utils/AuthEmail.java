package org.example.knockin.entity.utils;

import jakarta.persistence.*;
import lombok.*;
import org.example.knockin.entity.auth.AuthenticationType;
import org.example.knockin.global.jpa.BaseEntity;
import org.hibernate.annotations.ColumnDefault;

@Getter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "auth_email")
public class AuthEmail extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false, length = 100)
    private String domain;

    @Column(nullable = false, length = 100)
    private String name;

    @ColumnDefault(value = "false")
    private Boolean isDeleted;

    @Enumerated(EnumType.STRING)
    private AuthenticationType dtype;
}
