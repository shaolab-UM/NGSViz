package com.NGSViz.cli;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/** Writes computation manifests after the calculation log stream is closed. */
final class ComputeManifestWriter {
    void write(ComputeManifest manifest, Path path) throws IOException {
        Files.write(path, manifest.toJson().toString(4).getBytes(StandardCharsets.UTF_8));
    }
}
