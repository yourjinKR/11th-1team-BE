package org.example.knockin.global.util;

import java.time.LocalDate;
import java.time.Period;

public class DateUtils {
    private DateUtils() {}

    public static int calculateAge(LocalDate birth) {
        if (birth == null) {
            throw new IllegalArgumentException("생년월일(birth)은 null일 수 없습니다.");
        }

        LocalDate currentDate = LocalDate.now();

        if (birth.isAfter(currentDate)) {
            return 0;
        }

        return Period.between(birth, currentDate).getYears();
    }
}
