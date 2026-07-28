package com.NGSViz.sqldbOperate.exonMode;

import com.NGSViz.sqldbOperate.DBAtribute;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Benchen Ye
 * @create 2024-11--21:54
 */
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExonModelData extends DBAtribute {
    public static Map<String, Object> exon_matrix = new HashMap<>();

    public static void main(String[] args) {
        String table_name = "Homo_sapiens_ensembl_GRCh38_113";
        String analysis_type = "exon";
        String biotype = "protein_coding";
        getExonModelData(table_name, analysis_type, biotype);
    }

    public static void getExonModelData(String table_name, String analysis_type, String biotype) {
        ResultSet resultSet;
        String tid_name = "";
        int start_enst = 0;
        int end_enst = 0;
        int width_enst = 0;

        // Declare as null and instantiate when a new tid is found to prevent reusing a single instance.
        List<Integer> start_list = null;
        List<Integer> end_list = null;
        List<Integer> width_list = null;

        try {
            statement = initialDB();
            // Fix 3: Add "ORDER BY tid, start" to ensure data is grouped by transcript and sorted by 5' to 3' coordinates.
            String query = String.format("SELECT tid, start, end, width FROM %s " +
                    "WHERE type = '%s' AND biotype = '%s' ORDER BY tid, start", table_name, analysis_type, biotype);
            System.out.println("The query keyword is : " + query);
            resultSet = statement.executeQuery(query);

            while (resultSet.next()) {
                String query_tid = resultSet.getString("tid");
                int query_start = resultSet.getInt("start");
                int query_end = resultSet.getInt("end");
                int query_width = resultSet.getInt("width");

                if (!tid_name.equals(query_tid)) {
                    // Save the result of the previous transcript
                    if (!tid_name.equals("")) {
                        OneEnstAllExonCoorClass one_enst = new OneEnstAllExonCoorClass(tid_name, start_enst, end_enst,
                                width_enst, start_list, end_list, width_list);
                        exon_matrix.put(tid_name, one_enst);
                    }

                    // Fix 1: Create completely new List instances for the new transcript to eliminate reference sharing.
                    start_list = new ArrayList<>();
                    end_list = new ArrayList<>();
                    width_list = new ArrayList<>();

                    tid_name = query_tid;
                    start_enst = query_start;
                    // Fix 2: Correctly initialize end_enst upon encountering a new transcript.
                    end_enst = query_end;
                    width_enst = query_width;

                    start_list.add(query_start);
                    end_list.add(query_end);
                    width_list.add(query_width);
                } else {
                    // Process subsequent exons of the same transcript
                    // Use Math.max to improve robustness against potential coordinate overlaps.
                    end_enst = Math.max(end_enst, query_end);
                    width_enst += query_width;

                    start_list.add(query_start);
                    end_list.add(query_end);
                    width_list.add(query_width);
                }
            }
            // Save the very last result
            if (!tid_name.equals("")) {
                OneEnstAllExonCoorClass one_enst = new OneEnstAllExonCoorClass(tid_name, start_enst, end_enst,
                        width_enst, start_list, end_list, width_list);
                exon_matrix.put(tid_name, one_enst);
            }
        } catch (SQLException e) {
            System.out.println("Database read data error: " + e.getMessage());
        }
        exitDB();
    }

    // Subsequent Getters remain unchanged
    public static OneEnstAllExonCoorClass getEnstExonRes(String tid){
        return (OneEnstAllExonCoorClass) exon_matrix.get(tid);
    }
    public static int getEnstExonWidth(String tid){
        return getEnstExonRes(tid).getExonWholeWidth();
    }
    public static int getEnstStart(String tid){
        return getEnstExonRes(tid).getExonStart();
    }
    public static int getEnstEnd(String tid){
        return getEnstExonRes(tid).getExonEnd();
    }
    public static List<Integer> getExonStartList(String tid){
        return getEnstExonRes(tid).getStartList();
    }
    public static List<Integer> getExonEndList(String tid){
        return getEnstExonRes(tid).getEndList();
    }
}