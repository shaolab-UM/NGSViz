package com.NGSViz.compute;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Read-only annotation database inspection result. */
public final class DatabaseInspection {
    private final List<ValidationIssue> errors;
    private final String coordinateTable;
    private final Set<String> chromosomes;
    private final Map<String, List<String>> supportedValues;

    DatabaseInspection(List<ValidationIssue> errors, String coordinateTable,
                       Set<String> chromosomes, Map<String, List<String>> supportedValues) {
        this.errors = List.copyOf(errors);
        this.coordinateTable = coordinateTable;
        this.chromosomes = Set.copyOf(chromosomes);
        this.supportedValues = Collections.unmodifiableMap(supportedValues);
    }

    public List<ValidationIssue> errors() { return errors; }
    public String coordinateTable() { return coordinateTable; }
    public Set<String> chromosomes() { return chromosomes; }
    public Map<String, List<String>> supportedValues() { return supportedValues; }
}
