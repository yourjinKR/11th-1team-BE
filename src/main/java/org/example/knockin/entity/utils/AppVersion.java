package org.example.knockin.entity.utils;

import jakarta.persistence.*;
import lombok.*;
import org.example.knockin.global.entity.BaseEntity;
import org.hibernate.annotations.ColumnDefault;

@Getter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "app_version")
public class AppVersion extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false, length = 30)
    private String version;

    @Builder.Default
    @ColumnDefault(value = "false")
    private Boolean isDeleted = false;

    public void deleteAppVersion() {
        this.isDeleted = true;
    }

    public void modifyVersion(String version) {
        this.version = version;
    }
}
