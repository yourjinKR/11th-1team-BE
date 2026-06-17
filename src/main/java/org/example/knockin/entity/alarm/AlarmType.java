package org.example.knockin.entity.alarm;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AlarmType {
    NOTIFICATION("알림");

    private final String message;
}
