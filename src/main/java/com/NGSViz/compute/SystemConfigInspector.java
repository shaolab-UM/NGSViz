package com.NGSViz.compute;

import com.NGSViz.NGSViz;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/** Resolves system config, database and runtime JAR information without writes. */
final class SystemConfigInspector {
    SystemConfigInspection inspect(ComputeRequest request) {
        List<ValidationIssue> errors = new ArrayList<>();
        List<ValidationIssue> warnings = new ArrayList<>();
        Path configPath = request.systemConfig().map(Paths::get).orElseGet(this::defaultConfigPath);
        JSONObject details = new JSONObject().put("config_path", configPath.toString());
        if (!Files.isRegularFile(configPath) || !Files.isReadable(configPath)) {
            errors.add(issue("SYSTEM_CONFIG_UNREADABLE", "system_config",
                    "System config is not readable: " + configPath + "."));
            return new SystemConfigInspection(configPath, null, details, errors, warnings);
        }
        Path database = parseConfig(configPath, details, errors);
        JSONObject jar = jarInformation();
        details.put("jar", jar);
        warnOnJarMismatch(details, jar, warnings);
        return new SystemConfigInspection(configPath, database, details, errors, warnings);
    }

    private Path parseConfig(Path configPath, JSONObject details,
                             List<ValidationIssue> errors) {
        try {
            JSONObject root = new JSONObject(new String(
                    Files.readAllBytes(configPath), StandardCharsets.UTF_8
            ));
            JSONObject version = root.getJSONObject("versionNum");
            JSONObject tools = root.getJSONObject("toolParas");
            details.put("configured_version", version.getString("version"));
            details.put("configured_jar", tools.getString("tool_name"));
            details.put("java_path", tools.getString("java_path"));
            Path database = resolveDatabase(tools, configPath);
            details.put("database_path", database.toString());
            checkConfiguredPaths(tools, database, errors);
            return database;
        } catch (Exception error) {
            errors.add(issue("SYSTEM_CONFIG_INVALID", "system_config",
                    "System config is invalid: " + error.getMessage()));
            return null;
        }
    }

    private Path resolveDatabase(JSONObject tools, Path configPath) {
        Path database = Paths.get(tools.getString("db_path"));
        if (database.isAbsolute()) return database.normalize();
        Path toolPath = Paths.get(tools.optString(
                "tool_path", configPath.toAbsolutePath().getParent().toString()
        ));
        return toolPath.resolve(database).normalize();
    }

    private void checkConfiguredPaths(JSONObject tools, Path database,
                                      List<ValidationIssue> errors) {
        if (!Files.isRegularFile(database) || !Files.isReadable(database)) {
            errors.add(issue("DATABASE_UNREADABLE", "system_config",
                    "Configured database is not readable: " + database + "."));
        }
        Path javaPath = Paths.get(tools.getString("java_path"));
        if (!Files.isRegularFile(javaPath) || !Files.isExecutable(javaPath)) {
            errors.add(issue("JAVA_RUNTIME_UNAVAILABLE", "system_config",
                    "Configured Java executable is unavailable: " + javaPath + "."));
        }
    }

    private JSONObject jarInformation() {
        JSONObject jar = new JSONObject();
        try {
            Path location = Paths.get(NGSViz.class.getProtectionDomain()
                    .getCodeSource().getLocation().toURI()).toAbsolutePath();
            jar.put("location", location.toString());
            jar.put("file_name", location.getFileName().toString());
            jar.put("packaged_jar", Files.isRegularFile(location));
        } catch (URISyntaxException | RuntimeException error) {
            jar.put("location", JSONObject.NULL);
            jar.put("file_name", JSONObject.NULL);
            jar.put("packaged_jar", false);
        }
        return jar;
    }

    private void warnOnJarMismatch(JSONObject details, JSONObject jar,
                                   List<ValidationIssue> warnings) {
        if (!jar.optBoolean("packaged_jar") || !details.has("configured_jar")) return;
        if (!details.getString("configured_jar").equals(jar.getString("file_name"))) {
            warnings.add(issue("JAR_NAME_MISMATCH", "system_config",
                    "Configured JAR name differs from the running JAR."));
        }
    }

    private Path defaultConfigPath() {
        try {
            Path location = Paths.get(NGSViz.class.getProtectionDomain()
                    .getCodeSource().getLocation().toURI()).toAbsolutePath();
            Path parent = Files.isDirectory(location) ? Paths.get("").toAbsolutePath()
                    : location.getParent();
            return parent.resolve("NGSViz_setting.json");
        } catch (URISyntaxException | RuntimeException error) {
            return Paths.get("NGSViz_setting.json").toAbsolutePath();
        }
    }

    private ValidationIssue issue(String code, String field, String message) {
        return new ValidationIssue(code, field, message);
    }
}
