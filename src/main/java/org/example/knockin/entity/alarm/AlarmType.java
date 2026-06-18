package org.example.knockin.entity.alarm;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum AlarmType {
    DEFUALT(Values.DEFUALT),
    OFFER(Values.ROOM_MATCHING),
    CHATTING_REQUIRED(Values.CHATTING_REQUIRED);

    private final String value;

    public static class Values {
        public static final String DEFUALT = "SEEKER";
        public static final String ROOM_MATCHING = "ROOM_MATCHING";
        public static final String CHATTING_REQUIRED = "CHATTING_REQUIRED";
    }
}
