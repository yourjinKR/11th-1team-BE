package org.example.knockin.global.util;

import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StringUtils {
    private StringUtils() {}

    public static String parseToRegionFullName(
            String grandParentRegionName,
            String parentRegionName,
            String regionName
    ) {
        return Stream.of(grandParentRegionName, parentRegionName, regionName)
                .filter(Objects::nonNull)
                .filter(name -> !name.isBlank())
                .collect(Collectors.joining(" "));
    }
}
