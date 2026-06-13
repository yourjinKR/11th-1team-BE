package org.example.knockin.repository.member;

import org.example.knockin.dto.PopularSearchDto;

import java.util.List;

public interface SearchRepositoryCustom {
    List<PopularSearchDto.Response.RankItem> findPopSearch();
}