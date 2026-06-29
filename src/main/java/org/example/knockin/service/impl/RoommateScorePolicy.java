package org.example.knockin.service.impl;

import org.springframework.stereotype.Component;

@Component
public class RoommateScorePolicy {
    private static final int PERFECT_SCORE = 100;
    private static final int IMPORTANT_PATTERN_MULTIPLIER = 2;

    int importanceMultiplier(boolean important) {
        return important ? IMPORTANT_PATTERN_MULTIPLIER : 1;
    }

    int toScore(double rawScore, int maxRawScore) {
        if (maxRawScore <= 0) {
            return 0;
        }
        return clampScore((int) Math.round(rawScore / maxRawScore * PERFECT_SCORE));
    }

    int toPercent(double similarity) {
        return clampScore((int) Math.round(similarity * PERFECT_SCORE));
    }

    double clampSimilarity(double similarity) {
        return Math.max(0.0, Math.min(1.0, similarity));
    }

    private int clampScore(int score) {
        return Math.max(0, Math.min(PERFECT_SCORE, score));
    }
}
