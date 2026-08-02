package com.NGSViz.compute;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Validates an annotation database through a SQLite read-only connection. */
public final class DatabaseInspector {
    public DatabaseInspection inspect(Path database, String genome, String dbType,
                                      String region, String analysisType, String biotype) {
        List<ValidationIssue> errors = new ArrayList<>();
        Map<String, List<String>> supported = emptySupportedValues();
        Set<String> chromosomes = new LinkedHashSet<>();
        String coordinateTable = null;
        if (!Files.isRegularFile(database) || !Files.isReadable(database)) {
            errors.add(issue("DATABASE_UNREADABLE", "database", "Database is not readable."));
            return new DatabaseInspection(errors, null, chromosomes, supported);
        }
        try (Connection connection = openReadOnly(database)) {
            checkIntegrity(connection, errors);
            loadSupportedValues(connection, supported);
            coordinateTable = findCombination(
                    connection, genome, dbType, region, analysisType, biotype, errors
            );
            if (coordinateTable != null) {
                inspectCoordinateTable(connection, coordinateTable, chromosomes, errors);
            }
        } catch (SQLException error) {
            errors.add(issue("DATABASE_OPEN_FAILED", "database", error.getMessage()));
        }
        return new DatabaseInspection(errors, coordinateTable, chromosomes, supported);
    }

    private Connection openReadOnly(Path database) throws SQLException {
        String url = "jdbc:sqlite:file:" + database.toAbsolutePath() + "?mode=ro";
        return DriverManager.getConnection(url);
    }

    private void checkIntegrity(Connection connection, List<ValidationIssue> errors)
            throws SQLException {
        try (Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery("PRAGMA integrity_check")) {
            if (!result.next() || !"ok".equalsIgnoreCase(result.getString(1))) {
                errors.add(issue("DATABASE_INTEGRITY_FAILED", "database",
                        "SQLite integrity_check did not return ok."));
            }
        }
    }

    private void loadSupportedValues(Connection connection,
                                     Map<String, List<String>> supported) throws SQLException {
        supported.put("genomes", distinct(connection, "Genome"));
        supported.put("databases", distinct(connection, "DB"));
        supported.put("regions", distinct(connection, "Region"));
        supported.put("analysis_types", distinct(connection, "AnalysisType"));
        supported.put("biotypes", distinct(connection, "Biotype"));
    }

    private List<String> distinct(Connection connection, String column) throws SQLException {
        List<String> values = new ArrayList<>();
        String sql = "SELECT DISTINCT " + column + " FROM defaultTbl ORDER BY " + column;
        try (Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery(sql)) {
            while (result.next()) values.add(result.getString(1));
        }
        return values;
    }

    private String findCombination(Connection connection, String genome, String dbType,
                                   String region, String analysisType, String biotype,
                                   List<ValidationIssue> errors) throws SQLException {
        String sql = "SELECT CoordinateTblName FROM defaultTbl WHERE Genome=? AND DB=? "
                + "AND Region=? AND AnalysisType=? AND Biotype=? LIMIT 1";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, genome);
            statement.setString(2, dbType);
            statement.setString(3, region);
            statement.setString(4, analysisType);
            statement.setString(5, biotype);
            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) return result.getString(1);
            }
        }
        errors.add(issue("DATABASE_COMBINATION_NOT_FOUND", "genome",
                "Genome, database, region, analysis_type and biotype combination was not found."));
        return null;
    }

    private void inspectCoordinateTable(Connection connection, String table,
                                        Set<String> chromosomes,
                                        List<ValidationIssue> errors) throws SQLException {
        if (!tableExists(connection, table)) {
            errors.add(issue("DATABASE_COORDINATE_TABLE_MISSING", "database",
                    "Coordinate table does not exist: " + table + "."));
            return;
        }
        String quotedTable = '"' + table.replace("\"", "\"\"") + '"';
        try (Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery(
                     "SELECT DISTINCT chrom FROM " + quotedTable + " ORDER BY chrom")) {
            while (result.next()) chromosomes.add(result.getString(1));
        }
        if (chromosomes.isEmpty()) {
            errors.add(issue("DATABASE_CHROMOSOMES_EMPTY", "database",
                    "Coordinate table contains no chromosomes."));
        }
    }

    private boolean tableExists(Connection connection, String table) throws SQLException {
        String sql = "SELECT 1 FROM sqlite_master WHERE type='table' AND name=?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, table);
            try (ResultSet result = statement.executeQuery()) {
                return result.next();
            }
        }
    }

    private Map<String, List<String>> emptySupportedValues() {
        Map<String, List<String>> values = new LinkedHashMap<>();
        values.put("genomes", List.of());
        values.put("databases", List.of());
        values.put("regions", List.of());
        values.put("analysis_types", List.of());
        values.put("biotypes", List.of());
        return values;
    }

    private ValidationIssue issue(String code, String field, String message) {
        return new ValidationIssue(code, field, message);
    }
}
