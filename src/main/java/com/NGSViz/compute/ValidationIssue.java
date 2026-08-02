package com.NGSViz.compute;

import org.json.JSONObject;

/** A machine-readable validation warning or error. */
public final class ValidationIssue {
    private final String code;
    private final String field;
    private final String message;

    public ValidationIssue(String code, String field, String message) {
        this.code = code;
        this.field = field;
        this.message = message;
    }

    public String code() {
        return code;
    }

    public String field() {
        return field;
    }

    public String message() {
        return message;
    }

    public JSONObject toJson() {
        return new JSONObject()
                .put("code", code)
                .put("field", field == null ? JSONObject.NULL : field)
                .put("message", message);
    }
}
