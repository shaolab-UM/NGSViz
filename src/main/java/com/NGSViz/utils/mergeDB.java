package com.NGSViz.utils;

import com.NGSViz.configSet.InputParameterAttributes;
import java.sql.*;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Benchen Ye
 * @create 2025-01--22:47
 * @function merge new database into software's db
 * @function insert defaultTbl information and create new table including genome information
 */
public class mergeDB extends InputParameterAttributes {
    public static void main(String[] args) {
        String main_db_path = "/Users/benche/myProj/ngsPlot/NGSViz/database/genomeCoordinate.db";
        String input_db_path = "/Users/benche/myProj/ngsPlot/DB/DB/Mus_musculus/NGSViz_mm9_RefSeq.db";
        merge2DB(main_db_path, input_db_path);

    }

    public static void merge2DB(String main_db_path, String input_db_path){
        String tbl_name = checkUniqueTbl(main_db_path, input_db_path);
        if (tbl_name != null) {
            System.out.println("Creating tables...");
            dbTransfer(main_db_path, input_db_path, tbl_name);
        } else {
            System.out.println("Table already exists!");
        }
    }
    public static void dbTransfer(String main_db_path, String input_db_path, String tbl_name) {
        try (
                Connection conn = DriverManager.getConnection("jdbc:sqlite:" + main_db_path);
                // stmt run sql object
                Statement stmt = conn.createStatement()
        ) {
            // Attach input_db to the current connection
            stmt.execute("ATTACH DATABASE '" + input_db_path + "' AS source_db");
            // Merge the defaultTbl table (handle primary key conflicts)
            stmt.executeUpdate("INSERT OR IGNORE INTO main.defaultTbl SELECT * FROM source_db.defaultTbl");
            //
            // Verify if the table exists
            try (ResultSet rs = stmt.executeQuery(
                    "SELECT 1 FROM source_db.sqlite_master WHERE type='table' AND name='" + tbl_name + "'")) {
                if (!rs.next()) {
                    throw new SQLException("Origin " + tbl_name + " not found");
                }
            }
            // Retrieve the original table creation statement
            String createSQL;
            try (ResultSet rs = stmt.executeQuery(
                    "SELECT sql FROM source_db.sqlite_master WHERE type='table' AND name='" + tbl_name + "'")) {
                if (!rs.next()) throw new SQLException("Unable to obtain the table creation statement");
                createSQL = rs.getString("sql");
            }
            // Delete the target table (if it exists) and create a new table
            stmt.executeUpdate("DROP TABLE IF EXISTS main." + tbl_name);
            stmt.executeUpdate(createSQL.replace("CREATE TABLE", "CREATE TABLE IF NOT EXISTS"));
            // copy data
            stmt.executeUpdate("INSERT INTO main." + tbl_name +
                    " SELECT * FROM source_db." + tbl_name);
            // Uninstall additional database
            stmt.execute("DETACH DATABASE source_db");
            System.out.println("Database merge successful！");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static String checkUniqueTbl(String main_db_path, String input_db_path){
        // check whether the table exist
        String default_tbl = "defaultTbl";
        Set<String> ngs_tbl_name = new HashSet<>();
        Set<String> input_tbl_name = new HashSet<>();
        boolean genome_exist = false;
        String tbl_name = null;
        try (Connection ngsCon = DriverManager.getConnection("jdbc:sqlite:" + main_db_path);
             Connection inputCon = DriverManager.getConnection("jdbc:sqlite:" + input_db_path);
             Statement ngsStmt = ngsCon.createStatement();
             Statement inputStmt = inputCon.createStatement()) {
            // Query unique values in ngs_db
            ResultSet ngs_rs = ngsStmt.executeQuery("SELECT DISTINCT CoordinateTblName FROM " + default_tbl);
            while (ngs_rs.next()) {
                ngs_tbl_name.add(ngs_rs.getString("CoordinateTblName"));
                //System.out.println(ngs_rs.getString("CoordinateTblName"));
            }
            // Query unique values in input_db
            ResultSet input_rs = inputStmt.executeQuery("SELECT DISTINCT CoordinateTblName FROM " + default_tbl);
            while (input_rs.next()) {
                input_tbl_name.add(input_rs.getString("CoordinateTblName"));
                System.out.println(input_rs.getString("CoordinateTblName"));
            }
            // Determine whether the value in input_db is in ngs_db.
            for (String coordinate : input_tbl_name) {
                if (ngs_tbl_name.contains(coordinate)) {
                    //System.out.println(coordinate + " exists in ngs_db.");
                    genome_exist = true;
                }
            }
            if (!genome_exist) {
                tbl_name = input_tbl_name.iterator().next();
                //System.out.println(tbl_name);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return tbl_name;
    }
}