package com.NGSViz.sqldbOperate;

import java.sql.*;

/**
 * @author Benchen Ye
 * @create 2024-11--22:38
 * @function get the database name for max match query
 */
public class GetRefDB extends DBAtribute {

    public static String getDBTableName(String genome, String DB){
        String query_refname = null;
        try {
            initialDB();
            String query = String.format("SELECT DISTINCT CoordinateTblName FROM defaultTbl " +
                    "WHERE Genome = '%s' AND DB = '%s' ", genome, DB);
            //System.out.println("query key is: " + query);
            ResultSet resultSet = statement.executeQuery(query);
            if (resultSet.next()) {
                query_refname = resultSet.getString("CoordinateTblName");
            }
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
        System.out.println("the usage of genome coordinate database by query will be : " + query_refname);
        //System.out.println("---------");
        exitDB();
        return query_refname;
    }

    public static String getSpeciesName(String genome, String DB){
        String species_name = null;
        try {
            initialDB();
            String query = String.format("SELECT DISTINCT Species FROM defaultTbl " +
                    "WHERE Genome = '%s' AND DB = '%s' ", genome, DB);
            //System.out.println("query key is: " + query);
            ResultSet resultSet = statement.executeQuery(query);
            if (resultSet.next()) {
                species_name = resultSet.getString("Species");
            }
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
        System.out.println("the usage of species is : " + species_name);
        //System.out.println("---------");
        exitDB();
        return species_name;
    }

    public static String getTblName(String genome, String DB){
        String tbl_name = null;
        try {
            initialDB();
            String query = String.format("SELECT DISTINCT CoordinateTblName FROM defaultTbl " +
                    "WHERE Genome = '%s' AND DB = '%s' ", genome, DB);
            //System.out.println("query key is: " + query);
            ResultSet resultSet = statement.executeQuery(query);
            if (resultSet.next()) {
                tbl_name = resultSet.getString("CoordinateTblName");
            }
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
        System.out.println("the usage of table name is : " + tbl_name);
        System.out.println("---------");
        exitDB();
        return tbl_name;
    }
}
