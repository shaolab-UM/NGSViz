package com.NGSViz.cli;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

/** Paths accepted from a completed native computation. */
final class ComputeOutputs {
    private final List<String> coverageMatrices;
    private final List<String> readCountFiles;
    private final String plotSettingFile;

    ComputeOutputs(List<String> coverageMatrices, List<String> readCountFiles,
                   String plotSettingFile) {
        this.coverageMatrices = List.copyOf(coverageMatrices);
        this.readCountFiles = List.copyOf(readCountFiles);
        this.plotSettingFile = plotSettingFile;
    }

    List<String> coverageMatrices() { return coverageMatrices; }
    List<String> readCountFiles() { return readCountFiles; }
    String plotSettingFile() { return plotSettingFile; }

    JSONObject toJson() {
        return new JSONObject()
                .put("coverage_matrices", new JSONArray(coverageMatrices))
                .put("read_count_files", new JSONArray(readCountFiles))
                .put("plot_setting_file", plotSettingFile == null
                        ? JSONObject.NULL : plotSettingFile);
    }

    static ComputeOutputs empty() {
        return new ComputeOutputs(List.of(), List.of(), null);
    }
}
