package com.NGSViz.sqldbOperate;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertFalse;

class TranscriptApiTest {

    @Test
    void transcriptDoesNotExposeSharedQueryCoordinateMap() {
        boolean hasSharedRecord = Arrays.stream(Transcript.class.getDeclaredFields())
                .anyMatch(field -> field.getName().equals("record"));
        boolean hasQueryCoordMethod = Arrays.stream(Transcript.class.getDeclaredMethods())
                .anyMatch(method -> method.getName().equals("getQueryCoord"));

        assertFalse(hasSharedRecord, "Transcript must not retain a shared mutable record map");
        assertFalse(hasQueryCoordMethod, "Unused getQueryCoord API must be removed");
    }
}
