package org.example.knockin.repository.utils;

import org.example.knockin.entity.utils.AppVersion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppVersionRepository extends JpaRepository<AppVersion, Long> {
}
