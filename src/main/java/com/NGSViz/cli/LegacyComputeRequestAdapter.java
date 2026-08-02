package com.NGSViz.cli;

import com.NGSViz.compute.ComputeRequest;
import com.NGSViz.compute.ComputeRequestFormatException;
import com.NGSViz.compute.ComputeRequestReader;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Converts legacy flags to resolved single-sample requests without computing. */
public final class LegacyComputeRequestAdapter {
    public ComputeRequest fromArgs(String[] args) throws ComputeRequestFormatException {
        Map<String, String> flags = parseFlags(args);
        return buildRequest(flags, directMetadata(flags));
    }

    public List<ComputeRequest> fromArgsList(String[] args)
            throws ComputeRequestFormatException, IOException {
        Map<String, String> flags = parseFlags(args);
        String input = flags.get("-I");
        if (input == null || input.toLowerCase().endsWith(".bam")) {
            return List.of(buildRequest(flags, directMetadata(flags)));
        }
        return readBatch(flags, Path.of(input));
    }

    private Map<String, String> parseFlags(String[] args) {
        if (args.length == 0 || args.length % 2 != 0) {
            throw new IllegalArgumentException("Legacy computation flags require value pairs.");
        }
        Map<String, String> values = new LinkedHashMap<>();
        for (int index = 0; index < args.length; index += 2) {
            values.put(args[index], args[index + 1]);
        }
        return values;
    }

    private List<ComputeRequest> readBatch(Map<String, String> flags, Path input)
            throws IOException, ComputeRequestFormatException {
        List<String> lines = Files.readAllLines(input);
        int first = input.toString().toLowerCase().endsWith(".csv") ? 1 : 0;
        List<ComputeRequest> requests = new ArrayList<>();
        for (int index = first; index < lines.size(); index++) {
            String[] fields = lines.get(index).split(",");
            if (fields.length < 5) {
                throw new IllegalArgumentException("Legacy sample rows require five columns.");
            }
            Map<String, String> rowFlags = new LinkedHashMap<>(flags);
            rowFlags.put("-I", fields[1]);
            rowFlags.put("-X", fields[2]);
            rowFlags.put("-T", fields[4]);
            requests.add(buildRequest(rowFlags, new Metadata(fields[0], fields[0], fields[3])));
        }
        return requests;
    }

    private ComputeRequest buildRequest(Map<String, String> flags, Metadata metadata)
            throws ComputeRequestFormatException {
        JSONObject json = identity(flags, metadata);
        addAnalysis(json, flags);
        addExecution(json, flags);
        return new ComputeRequestReader().read(json);
    }

    private JSONObject identity(Map<String, String> flags, Metadata metadata) {
        String[] bamPair = required(flags, "-I").split(":", 2);
        return new JSONObject()
                .put("schema_version", "1.3")
                .put("sample_id", metadata.sampleId)
                .put("sample_name", metadata.sampleName)
                .put("group_name", metadata.groupName)
                .put("title", required(flags, "-T"))
                .put("genome", required(flags, "-G"))
                .put("region", required(flags, "-R"))
                .put("signal_bam", bamPair[0])
                .put("control_bam", bamPair.length == 2 ? bamPair[1] : JSONObject.NULL)
                .put("output_dir", required(flags, "-O"))
                .put("system_config", nullable(flags.get("-CP")));
    }

    private void addAnalysis(JSONObject json, Map<String, String> flags) {
        int flank = integer(flags, "-F", 0);
        json.put("database", flags.getOrDefault("-D", "RefSeq"))
                .put("analysis_type", flags.getOrDefault("-A", "transcript"))
                .put("biotype", flags.getOrDefault("-B", "protein_coding"))
                .put("flank_region", flank == 0 ? JSONObject.NULL : flank)
                .put("flank_factor", decimal(flags, "-N", 0.0))
                .put("num_datapoints", integer(flags, "-DP", 100))
                .put("scale_ratio", optionalDecimal(flags, "-S"));
    }

    private void addExecution(JSONObject json, Map<String, String> flags) {
        json.put("mapping_quality", integer(flags, "-MQ", 20))
                .put("fragment_length", integer(flags, "-FL", 150))
                .put("cores", integer(flags, "-P", 1))
                .put("batch_size", integer(flags, "-BS", 500))
                .put("bin_method", flags.getOrDefault("-BM", "mean"))
                .put("strand_specific", flags.getOrDefault("-SS", "both"))
                .put("center_mode", bool(flags, "-CM", false))
                .put("custom_bed", nullable(flags.get("-BD")))
                .put("gene_subset", flags.getOrDefault("-X", "all"))
                .put("create_result_folder", bool(flags, "-NF", false));
    }

    private Metadata directMetadata(Map<String, String> flags) {
        String signal = required(flags, "-I").split(":", 2)[0];
        String name = Path.of(signal).getFileName().toString();
        String sampleId = name.toLowerCase().endsWith(".bam")
                ? name.substring(0, name.length() - 4) : name;
        return new Metadata(sampleId, "SampleName", "GroupName");
    }

    private String required(Map<String, String> flags, String flag) {
        String value = flags.get(flag);
        if (value == null) throw new IllegalArgumentException("Missing legacy flag: " + flag);
        return value;
    }

    private int integer(Map<String, String> flags, String flag, int fallback) {
        return flags.containsKey(flag) ? Integer.parseInt(flags.get(flag)) : fallback;
    }

    private double decimal(Map<String, String> flags, String flag, double fallback) {
        return flags.containsKey(flag) ? Double.parseDouble(flags.get(flag)) : fallback;
    }

    private Object optionalDecimal(Map<String, String> flags, String flag) {
        return flags.containsKey(flag) ? Double.parseDouble(flags.get(flag)) : JSONObject.NULL;
    }

    private boolean bool(Map<String, String> flags, String flag, boolean fallback) {
        return flags.containsKey(flag) ? Boolean.parseBoolean(flags.get(flag)) : fallback;
    }

    private Object nullable(String value) {
        return value == null || value.isEmpty() || "-".equals(value) ? JSONObject.NULL : value;
    }

    private static final class Metadata {
        private final String sampleId;
        private final String sampleName;
        private final String groupName;

        private Metadata(String sampleId, String sampleName, String groupName) {
            this.sampleId = sampleId;
            this.sampleName = sampleName;
            this.groupName = groupName;
        }
    }
}
