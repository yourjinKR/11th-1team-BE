package org.example.knockin.service.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.example.knockin.dto.Compatibility;
import org.example.knockin.entity.life.LifePatternType;
import org.example.knockin.repository.life.LifePatternInformationRepository;
import org.example.knockin.repository.life.MemberLifePatternRepository;
import org.example.knockin.repository.life.PreferenceConditionRepository;
import org.example.knockin.repository.life.PreferenceConditionWeightRepository;
import org.example.knockin.repository.life.row.LifePatternInformationValueRow;
import org.example.knockin.repository.life.row.MatchingLifestyleRow;
import org.example.knockin.repository.life.row.MatchingPreferenceConditionRow;
import org.example.knockin.repository.life.row.MatchingPreferenceConditionWeightRow;
import org.example.knockin.service.RoommateScoreService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JavaRoommateScoreService implements RoommateScoreService {
    private final MemberLifePatternRepository memberLifePatternRepository;
    private final PreferenceConditionRepository preferenceConditionRepository;
    private final PreferenceConditionWeightRepository preferenceConditionWeightRepository;
    private final LifePatternInformationRepository lifePatternInformationRepository;
    private final RoommateScorePolicy scorePolicy;

    @Override
    public Map<Long, Compatibility> calculateScores(Long requesterId, List<Long> targetMemberIds) {
        if (requesterId == null || targetMemberIds == null || targetMemberIds.isEmpty()) return Map.of();

        List<Long> memberIds = includeRequester(targetMemberIds, requesterId);
        List<MatchingLifestyleRow> lifestyleRows = memberLifePatternRepository.findAllLifestyleByMemberIdIn(memberIds);
        List<MatchingPreferenceConditionRow> conditionRows = preferenceConditionRepository.findAllPreferenceConditionByMemberIdIn(memberIds);
        List<MatchingPreferenceConditionWeightRow> conditionWeightRows = preferenceConditionWeightRepository.findAllPreferenceConditionWeightByMemberIdIn(memberIds);

        return calculateScores(requesterId, targetMemberIds, lifestyleRows, conditionRows, conditionWeightRows);
    }

    private Map<Long, Compatibility> calculateScores(
            Long requesterId,
            List<Long> targetMemberIds,
            List<MatchingLifestyleRow> lifestyleRows,
            List<MatchingPreferenceConditionRow> conditionRows,
            List<MatchingPreferenceConditionWeightRow> conditionWeightRows
    ) {
        if (requesterId == null || targetMemberIds == null || targetMemberIds.isEmpty()) return Map.of();

        Map<Long, Map<Long, MatchingLifestyleRow>> lifestylesByMemberId = lifestyleRows.stream()
                .filter(row -> row.lifePatternId() != null)
                .collect(Collectors.groupingBy(
                        MatchingLifestyleRow::memberId,
                        LinkedHashMap::new,
                        Collectors.toMap(
                                MatchingLifestyleRow::lifePatternId,
                                Function.identity(),
                                (first, ignored) -> first,
                                LinkedHashMap::new
                        )
                ));

        Map<Long, MatchingLifestyleRow> requesterLifestyles = lifestylesByMemberId.getOrDefault(requesterId, Map.of());
        if (requesterLifestyles.isEmpty()) return Map.of();

        Map<Long, List<MatchingPreferenceConditionRow>> requesterConditionsByPatternId = conditionRows.stream()
                .filter(row -> Objects.equals(row.memberId(), requesterId))
                .filter(row -> row.lifePatternId() != null)
                .collect(Collectors.groupingBy(MatchingPreferenceConditionRow::lifePatternId));

        Set<Long> requesterImportantPatternIds = conditionWeightRows.stream()
                .filter(row -> Objects.equals(row.memberId(), requesterId))
                .map(MatchingPreferenceConditionWeightRow::lifePatternId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Long, ScaleRange> scaleRangesByPatternId = findScaleRanges(requesterLifestyles.values());
        int maxRawScore = requesterLifestyles.size() + (int) requesterImportantPatternIds.stream().filter(requesterLifestyles::containsKey).count();

        if (maxRawScore == 0) return Map.of();

        return targetMemberIds.stream()
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toMap(
                        Function.identity(),
                        targetMemberId -> calculateCompatibility(
                                requesterLifestyles,
                                requesterConditionsByPatternId,
                                requesterImportantPatternIds,
                                lifestylesByMemberId.getOrDefault(targetMemberId, Map.of()),
                                scaleRangesByPatternId,
                                maxRawScore
                        )
                ));
    }

    @Override
    public Map<Long, Integer> calculateSimpleScores(Long requesterId, List<Long> targetMemberIds) {
        return toSimpleScores(calculateScores(requesterId, targetMemberIds));
    }

    @Override
    public Compatibility calculateScore(Long requesterId, Long targetMemberId) {
        if (targetMemberId == null) return null;
        return calculateScores(requesterId, List.of(targetMemberId)).get(targetMemberId);
    }

    @Override
    public Integer calculateSimpleScore(Long requesterId, Long targetMemberId) {
        Compatibility compatibility = calculateScore(requesterId, targetMemberId);
        return compatibility == null ? null : compatibility.getScore();
    }

    private Map<Long, Integer> toSimpleScores(Map<Long, Compatibility> scores) {
        return scores.entrySet().stream()
                .filter(entry -> entry.getValue() != null)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().getScore()
                ));
    }

    private Compatibility calculateCompatibility(
            Map<Long, MatchingLifestyleRow> requesterLifestyles,
            Map<Long, List<MatchingPreferenceConditionRow>> requesterConditionsByPatternId,
            Set<Long> requesterImportantPatternIds,
            Map<Long, MatchingLifestyleRow> targetLifestyles,
            Map<Long, ScaleRange> scaleRangesByPatternId,
            int maxRawScore
    ) {
        double rawScore = 0.0;
        List<Compatibility.LifeStyleInfo> lifeStyleInfo = new ArrayList<>();

        for (Map.Entry<Long, MatchingLifestyleRow> entry : requesterLifestyles.entrySet()) {
            Long lifePatternId = entry.getKey();
            MatchingLifestyleRow requesterLifestyle = entry.getValue();
            MatchingLifestyleRow targetLifestyle = targetLifestyles.get(lifePatternId);
            double similarity = 0.0;

            if (targetLifestyle != null) {
                similarity = calculateSimilarity(
                        requesterLifestyle,
                        requesterConditionsByPatternId.getOrDefault(lifePatternId, List.of()),
                        targetLifestyle,
                        scaleRangesByPatternId.get(lifePatternId)
                );
            }

            int importanceMultiplier = scorePolicy.importanceMultiplier(requesterImportantPatternIds.contains(lifePatternId));
            rawScore += similarity * importanceMultiplier;
            lifeStyleInfo.add(new Compatibility.LifeStyleInfo(
                    requesterLifestyle.name(),
                    scorePolicy.toPercent(similarity)
            ));
        }

        Integer score = scorePolicy.toScore(rawScore, maxRawScore);
        return new Compatibility(score, lifeStyleInfo);
    }

    private double calculateSimilarity(
            MatchingLifestyleRow requesterLifestyle,
            List<MatchingPreferenceConditionRow> requesterConditions,
            MatchingLifestyleRow targetLifestyle,
            ScaleRange scaleRange
    ) {
        LifePatternType type = targetLifestyle.type();

        if (type == LifePatternType.SINGLE_CHOICE) return calculateSingleChoiceSimilarity(requesterLifestyle, requesterConditions, targetLifestyle);
        if (type == LifePatternType.BOOLEAN) return calculateBooleanSimilarity(requesterLifestyle, requesterConditions, targetLifestyle);
        if (type == LifePatternType.SCALE) return calculateScaleSimilarity(requesterLifestyle, requesterConditions, targetLifestyle, scaleRange);

        return 0.0;
    }

    private double calculateSingleChoiceSimilarity(
            MatchingLifestyleRow requesterLifestyle,
            List<MatchingPreferenceConditionRow> requesterConditions,
            MatchingLifestyleRow targetLifestyle
    ) {
        if (requesterConditions.isEmpty()) {
            return Objects.equals(
                    requesterLifestyle.lifePatternInformationId(),
                    targetLifestyle.lifePatternInformationId()
            ) ? 1.0 : 0.0;
        }

        boolean matched = requesterConditions.stream()
                .map(MatchingPreferenceConditionRow::lifePatternInformationId)
                .anyMatch(conditionValueId -> Objects.equals(conditionValueId, targetLifestyle.lifePatternInformationId()));

        return matched ? 1.0 : 0.0;
    }

    private double calculateBooleanSimilarity(
            MatchingLifestyleRow requesterLifestyle,
            List<MatchingPreferenceConditionRow> requesterConditions,
            MatchingLifestyleRow targetLifestyle
    ) {
        String criteriaValue = requesterConditions.stream()
                .findFirst()
                .map(MatchingPreferenceConditionRow::value)
                .orElse(requesterLifestyle.value());

        return Objects.equals(criteriaValue, targetLifestyle.value()) ? 1.0 : 0.0;
    }

    private double calculateScaleSimilarity(
            MatchingLifestyleRow requesterLifestyle,
            List<MatchingPreferenceConditionRow> requesterConditions,
            MatchingLifestyleRow targetLifestyle,
            ScaleRange scaleRange
    ) {
        Integer criteriaValue = parseInteger(
                requesterConditions.stream()
                        .findFirst()
                        .map(MatchingPreferenceConditionRow::value)
                        .orElse(requesterLifestyle.value())
        );
        Integer targetValue = parseInteger(targetLifestyle.value());

        if (criteriaValue == null || targetValue == null || scaleRange == null || scaleRange.maxGap() <= 0) {
            return Objects.equals(criteriaValue, targetValue) ? 1.0 : 0.0;
        }

        double similarity = 1.0 - ((double) Math.abs(criteriaValue - targetValue) / scaleRange.maxGap());
        return scorePolicy.clampSimilarity(similarity);
    }

    private Map<Long, ScaleRange> findScaleRanges(Collection<MatchingLifestyleRow> requesterLifestyles) {
        List<Long> scalePatternIds = requesterLifestyles.stream()
                .filter(row -> row.type() == LifePatternType.SCALE)
                .map(MatchingLifestyleRow::lifePatternId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        if (scalePatternIds.isEmpty()) return Map.of();

        Map<Long, List<Integer>> valuesByPatternId = lifePatternInformationRepository
                .findAllValueRowsByLifePatternIdIn(scalePatternIds)
                .stream()
                .collect(Collectors.groupingBy(
                        LifePatternInformationValueRow::lifePatternId,
                        Collectors.mapping(
                                row -> parseInteger(row.value()),
                                Collectors.filtering(Objects::nonNull, Collectors.toList())
                        )
                ));

        return valuesByPatternId.entrySet().stream()
                .filter(entry -> !entry.getValue().isEmpty())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> {
                            List<Integer> values = entry.getValue();
                            int min = values.stream().min(Comparator.naturalOrder()).orElse(0);
                            int max = values.stream().max(Comparator.naturalOrder()).orElse(0);
                            return new ScaleRange(min, max);
                        }
                ));
    }

    private Integer parseInteger(String value) {
        try {
            return value == null ? null : Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private List<Long> includeRequester(List<Long> targetMemberIds, Long requesterId) {
        return Stream.concat(targetMemberIds.stream(), Stream.of(requesterId))
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }

    private record ScaleRange(int min, int max) {
        int maxGap() {
            return max - min;
        }
    }
}
