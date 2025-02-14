package sqldbOperate;

import java.sql.*;
import java.util.*;

/**
 * @author Benchen Ye
 * @create 2024-11--22:38
 * @function get the database name for max match query
 */
public class GetRefDB extends DBAtribute {
    public static void main(String[] args) {
        String query_genome = "GRCh38";
        String query_region = "genebody";
        String query_db = "ensembl";
        String res = getDBTableName(query_genome, query_db);
        //String res = getSpeciesName(query_genome, query_region, query_db);
        System.out.println("query result is : " + res);
        getSpeciesName( query_genome,  query_db);
    }
    public static String getDBTableName(String genome, String DB){
        String query_refname = null;
        try {
            initialDB();
            String query = String.format("SELECT DISTINCT CoordinateTblName FROM defaultTbl " +
                    "WHERE Genome = '%s' AND DB = '%s' ", genome, DB);
            System.out.println("query key is: " + query);
            ResultSet resultSet = statement.executeQuery(query);
            if (resultSet.next()) {
                query_refname = resultSet.getString("CoordinateTblName");
            }
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
        System.out.println("the usage of genome coordinate database by query will be : " + query_refname);
        System.out.println("---------");
        exitDB();
        return query_refname;
    }
    // --- delete ---
    /*public static String getDBFISubset2(List<String> query_list, String genome, String region2plot, String DB){
        String query_refname = null;
        int max_count = 0;
        int max_db_score = 0;
        try {
            initialDB();
            String query = String.format("SELECT refname,FI1,FI2,FI3,dbScore FROM refmappinginfotab " +
                   "WHERE Genome = '%s' AND Region = '%s' AND DB = '%s' ", genome, region2plot, DB);
            System.out.println("query key is: " + query);
            ResultSet resultSet = statement.executeQuery(query);
            while (resultSet.next()) {
                // get the items from database table with getString/getInt
                String FI1_res = resultSet.getString("FI1");
                String FI2_res = resultSet.getString("FI2");
                String FI3_res = resultSet.getString("FI3");
                String ref_name = resultSet.getString("refname");
                int db_score = resultSet.getInt("dbScore");
                List<String> db_list = new ArrayList<>(Arrays.asList(FI1_res, FI2_res, FI3_res));
                int fi_score  = getRefDBName(query_list, db_list);
                if(fi_score > max_count){
                    max_db_score = db_score;
                    max_count = fi_score;
                    query_refname = ref_name;
                }else if (fi_score == max_count){
                    if(db_score > max_db_score){
                        max_db_score = db_score;
                        max_count = fi_score;
                        query_refname = ref_name;
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
        System.out.println("the usage of genome coordinate database by query will be : " + query_refname);
        exitDB();
        return query_refname;
    }*/

    /*public static int getRefDBName(List<String> query_list, List<String> db_list){
            long count = db_list.stream()
                    .filter(query_list::contains)
                    // Calculate the number of intersections
                    .count();
            return (int) count;
    }*/
    public static String getSpeciesName(String genome, String DB){
        String species_name = null;
        try {
            initialDB();
            String query = String.format("SELECT DISTINCT Species FROM defaultTbl " +
                    "WHERE Genome = '%s' AND DB = '%s' ", genome, DB);
            System.out.println("query key is: " + query);
            ResultSet resultSet = statement.executeQuery(query);
            if (resultSet.next()) {
                species_name = resultSet.getString("Species");
            }
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
        System.out.println("the usage of species is : " + species_name);
        System.out.println("---------");
        exitDB();
        return species_name;
    }
    /*public static String getSpeciesName(String genome, String region2plot, String DB){
        String species_name = null;
        try {
            initialDB();
            String query = String.format("SELECT DISTINCT species FROM refmappinginfotab " +
                    "WHERE Genome = '%s' AND Region = '%s' AND DB = '%s' ", genome, region2plot, DB);
            System.out.println("query key is: " + query);
            ResultSet resultSet = statement.executeQuery(query);
            if (resultSet.next()) {
                species_name = resultSet.getString("species");
            }
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
        System.out.println("the usage of table name for species is : " + species_name);
        exitDB();
        return species_name;
    }*/
    public static String getTblName(String genome, String DB){
        String tbl_name = null;
        try {
            initialDB();
            String query = String.format("SELECT DISTINCT CoordinateTblName FROM defaultTbl " +
                    "WHERE Genome = '%s' AND DB = '%s' ", genome, DB);
            System.out.println("query key is: " + query);
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
