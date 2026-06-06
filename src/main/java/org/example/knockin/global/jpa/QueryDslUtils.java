package org.example.knockin.global.jpa;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.NumberPath;

public class QueryDslUtils {
    private QueryDslUtils() { }

    public static <T extends Number & Comparable<?>> BooleanExpression numberBetween(
            NumberPath<T> path, T min, T max) {

        if (min == null && max == null) {
            return null;
        }
        if (min != null && max != null) {
            return path.between(min, max);
        }
        if (min != null) {
            return path.goe(min);
        }
        return path.loe(max);
    }

}
