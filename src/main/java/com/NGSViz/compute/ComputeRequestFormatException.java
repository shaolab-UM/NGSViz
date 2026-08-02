package com.NGSViz.compute;

import java.util.List;

/** Indicates that a JSON request cannot be represented by the CLI contract. */
public final class ComputeRequestFormatException extends Exception {
    private final List<ValidationIssue> issues;

    public ComputeRequestFormatException(List<ValidationIssue> issues) {
        super(issues.isEmpty() ? "Invalid compute request." : issues.get(0).message());
        this.issues = List.copyOf(issues);
    }

    public List<ValidationIssue> issues() {
        return issues;
    }
}
