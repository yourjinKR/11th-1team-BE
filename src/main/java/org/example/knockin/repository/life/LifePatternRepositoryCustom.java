package org.example.knockin.repository.life;

import org.example.knockin.dto.MetaLifestylePatternsDto;

import java.util.List;

public interface LifePatternRepositoryCustom {
    List<MetaLifestylePatternsDto.Response.PatternItem>findLifeStylePatterns();
}