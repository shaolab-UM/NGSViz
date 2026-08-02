package com.NGSViz.cli;

import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CliResponseTest {
    private static final Set<String> REQUIRED_FIELDS = Set.of(
            "schema_version", "command", "status", "exit_code",
            "message", "data", "warnings", "errors"
    );

    @Test
    void jsonContainsEveryRequiredResponseField() {
        JSONObject response = CliResponse.success(
                "describe", "CLI contract described.", new JSONObject()
        ).toJson();

        assertTrue(response.keySet().containsAll(REQUIRED_FIELDS));
        assertEquals("1.0", response.getString("schema_version"));
        assertEquals(0, response.getInt("exit_code"));
        assertEquals(0, response.getJSONArray("warnings").length());
        assertEquals(0, response.getJSONArray("errors").length());
    }
}
