package org.example.knockin.repository.life;

import org.example.knockin.entity.life.LifePattern;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LifePatternRepository extends JpaRepository<LifePattern, Long> {
}