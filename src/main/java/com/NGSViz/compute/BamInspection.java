package com.NGSViz.compute;

import java.util.List;
import java.util.Set;

/** BAM readability, header, index and reference inspection result. */
public final class BamInspection {
    private final List<ValidationIssue> errors;
    private final Set<String> references;

    BamInspection(List<ValidationIssue> errors, Set<String> references) {
        this.errors = List.copyOf(errors);
        this.references = Set.copyOf(references);
    }

    public List<ValidationIssue> errors() { return errors; }
    public Set<String> references() { return references; }
}
