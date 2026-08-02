package com.NGSViz.compute;

import htsjdk.samtools.SAMFileHeader;
import htsjdk.samtools.SAMSequenceRecord;
import htsjdk.samtools.SamReader;
import htsjdk.samtools.SamReaderFactory;
import htsjdk.samtools.ValidationStringency;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/** Performs read-only BAM, BAI and reference dictionary inspection. */
public final class BamInspector {
    public BamInspection inspect(Path bam, String field) {
        List<ValidationIssue> errors = new ArrayList<>();
        Set<String> references = new LinkedHashSet<>();
        if (!Files.isRegularFile(bam) || !Files.isReadable(bam)) {
            errors.add(issue("BAM_UNREADABLE", field, "BAM is not readable: " + bam + "."));
            return new BamInspection(errors, references);
        }
        SamReaderFactory factory = SamReaderFactory.makeDefault()
                .validationStringency(ValidationStringency.STRICT);
        try (SamReader reader = factory.open(bam)) {
            inspectHeader(reader.getFileHeader(), field, references, errors);
            if (!reader.hasIndex()) {
                errors.add(issue("BAM_INDEX_MISSING", field,
                        "BAM index is missing or unusable: " + bam + "."));
            }
        } catch (IOException | RuntimeException error) {
            errors.add(issue("BAM_OPEN_FAILED", field, "Cannot open BAM: "
                    + error.getMessage()));
        }
        return new BamInspection(errors, references);
    }

    private void inspectHeader(SAMFileHeader header, String field, Set<String> references,
                               List<ValidationIssue> errors) {
        if (header == null || header.getSequenceDictionary() == null
                || header.getSequenceDictionary().isEmpty()) {
            errors.add(issue("BAM_HEADER_MISSING", field,
                    "BAM header has no reference sequence dictionary."));
            return;
        }
        for (SAMSequenceRecord sequence : header.getSequenceDictionary().getSequences()) {
            references.add(sequence.getSequenceName());
        }
    }

    public static boolean referenceNamingMismatch(Set<String> first, Set<String> second) {
        if (first.isEmpty() || second.isEmpty()) return false;
        return usesChrPrefix(first) != usesChrPrefix(second);
    }

    static boolean usesChrPrefix(Set<String> references) {
        int prefixed = 0;
        for (String reference : references) {
            if (reference.toLowerCase().startsWith("chr")) prefixed++;
        }
        return prefixed * 2 >= references.size();
    }

    static Set<String> normalizedReferences(Set<String> references) {
        Set<String> normalized = new LinkedHashSet<>();
        for (String reference : references) {
            String value = reference.toLowerCase();
            if (value.startsWith("chr")) value = value.substring(3);
            if ("m".equals(value)) value = "mt";
            normalized.add(value);
        }
        return normalized;
    }

    private ValidationIssue issue(String code, String field, String message) {
        return new ValidationIssue(code, field, message);
    }
}
