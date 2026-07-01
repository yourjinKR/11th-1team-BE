package org.example.knockin.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.knockin.dto.BoLifeStylePatternDetailDto;
import org.example.knockin.dto.BoLifeStylePatternListDto;
import org.example.knockin.dto.MetaLifestylePatternsDto;
import org.example.knockin.entity.life.LifePattern;
import org.example.knockin.entity.life.LifePatternInformation;
import org.example.knockin.global.exception.BusinessException;
import org.example.knockin.global.exception.LifePatternErrorCode;
import org.example.knockin.repository.life.LifePatternInformationRepository;
import org.example.knockin.repository.life.LifePatternRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LifeStyleServiceImpl {
    private final LifePatternRepository lifePatternRepository;
    private final LifePatternInformationRepository lifePatternInformationRepository;

    @Transactional
    public LifePattern saveLifePattern(LifePattern lifePattern) {
        return lifePatternRepository.save(lifePattern);
    }

    @Transactional
    public List<LifePatternInformation> saveLifePatternInformation(List<LifePatternInformation> lifePatternInformation) {
        return lifePatternInformationRepository.saveAll(lifePatternInformation);
    }

    @Transactional
    public LifePatternInformation saveLifeInformation(LifePatternInformation lifePatternInformation) {
        return lifePatternInformationRepository.save(lifePatternInformation);
    }

    @Transactional
    public LifePattern deleteLifePattern(Long patternId) {
        LifePattern lifePattern = lifePatternRepository.findById(patternId).orElseThrow(() -> new BusinessException(LifePatternErrorCode.LIFE_PATTERN_NOT_FOUNT));
        lifePattern.deleteLifePattern();
        return lifePattern;
    }

    @Transactional
    public void deleteLifeInformationByPattern(LifePattern lifePattern) {
        lifePatternInformationRepository.deleteByLifePattern(lifePattern);
    }

    public BoLifeStylePatternListDto.Response findLifeStylePatternList(Pageable pageable) {
        List<BoLifeStylePatternListDto.Response.PatternItem> patternItemList = lifePatternRepository.findLifeStylePatternList(pageable);
        return BoLifeStylePatternListDto.Response.builder().patterns(patternItemList).build();
    }

    public BoLifeStylePatternDetailDto.Response findLifeStylePattern(Long patternId) {
        return lifePatternRepository.findLifeStylePattern(patternId);
    }

    public LifePattern findLifeStyle(Long patternId) {
        return lifePatternRepository.findById(patternId).orElseThrow(() -> new BusinessException(LifePatternErrorCode.LIFE_PATTERN_NOT_FOUNT));
    }

    public List<LifePattern> findAllById(List<Long> lifeStyles) {
        return lifePatternRepository.findAllById(lifeStyles);
    }

    public List<LifePatternInformation> findByLifeStyles(List<Long> lifeStyles) {
        return lifePatternInformationRepository.findByLifeStyles(lifeStyles);
    }

    public List<MetaLifestylePatternsDto.Response.PatternItem> findLifeStylePatterns() {
        return lifePatternRepository.findLifeStylePatterns();
    }

    public List<LifePatternInformation> findLifePatternInformationAllById(List<Long> lifestyleIds) {
        return lifePatternInformationRepository.findAllById(lifestyleIds);
    }

    public LifePatternInformation findLifePatternInformationById(Long lifestyleId) {
        return lifePatternInformationRepository.findById(lifestyleId).orElse(null);
    }
}
