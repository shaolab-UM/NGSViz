package sqldbOperate.exonMode;

import sqldbOperate.DBAtribute;

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
public class ExonModelData extends DBAtribute {
    public static Map<String, Object> exon_matrix = new HashMap<>();
    public static void main(String [] args){
        String species = "human";
        String db_name = "ensembl";
        String genome = "hg19";
        getExonModelData(genome, db_name, species);
    }
    public static void getExonModelData(String genome, String db_name, String species){
        ResultSet resultSet;
        String region = "exonModel";
        String table_name = species + region;
        String tid_name = "";
        int start_enst = 0;
        int end_enst = 0;
        int width_enst = 0;
        List<Integer> start_list = new ArrayList<Integer>();
        List<Integer> end_list = new ArrayList<Integer>();
        List<Integer> width_list = new ArrayList<Integer>();
        try {
            statement = initialDB();
            String query = String.format("SELECT tid,start,end,width FROM %s " +
                    "WHERE Genome = '%s' AND db = '%s'", table_name, genome, db_name);
            System.out.println("The query keyword is : " + query);
            resultSet = statement.executeQuery(query);

            while (resultSet.next()) {
                // get item of tid,start,end,width
                String query_tid = resultSet.getString("tid");
                int query_start = resultSet.getInt("start");
                int query_end = resultSet.getInt("end");
                int query_width = resultSet.getInt("width");
                if(!tid_name.equals(query_tid)){
                    // process each enst result
                    if(!tid_name.equals("")){
                        OneEnstAllExonCoorClass one_enst = new OneEnstAllExonCoorClass(tid_name, start_enst, end_enst,
                                width_enst, start_list, end_list, width_list);
                        exon_matrix.put(tid_name, one_enst);
                    }
                    // new enst
                    start_list.clear();
                    end_list.clear();
                    width_list.clear();
                    tid_name = query_tid;
                    start_enst = query_start;
                    width_enst = query_width;
                    //
                    start_list.add(query_start);
                    end_list.add(query_end);
                    width_list.add(query_width);
                } else {
                    // process new enst result
                    end_enst = query_end;
                    width_enst = width_enst + query_width;
                    //
                    start_list.add(query_start);
                    end_list.add(query_end);
                    width_list.add(query_width);
                }
            }
            // the last result
            if(!tid_name.equals("")){
                OneEnstAllExonCoorClass one_enst = new OneEnstAllExonCoorClass(tid_name, start_enst, end_enst,
                        width_enst, start_list, end_list, width_list);
                exon_matrix.put(tid_name, one_enst);
            }
        } catch (SQLException e) {
            System.out.println("Database read data error: " + e.getMessage());
        }
        exitDB();
        //return query_res;
    }
    //
    public static OneEnstAllExonCoorClass getEnstExonRes(String tid){
        OneEnstAllExonCoorClass tid_res = (OneEnstAllExonCoorClass) exon_matrix.get(tid);
        return tid_res;
    }
    public static int getEnstExonWidth(String tid){
        OneEnstAllExonCoorClass tid_res = getEnstExonRes(tid);
        int exon_whole_width = tid_res.getExonWholeWidth();
        return exon_whole_width;
    }
    public static int getEnstStart(String tid){
        OneEnstAllExonCoorClass tid_res = getEnstExonRes(tid);
        int enst_start = tid_res.getExonStart();
        return enst_start;
    }
    public static int getEnstEnd(String tid){
        OneEnstAllExonCoorClass tid_res = getEnstExonRes(tid);
        int enst_end = tid_res.getExonEnd();
        return enst_end;
    }
    public static List<Integer> getExonStartList(String tid){
        OneEnstAllExonCoorClass tid_res = getEnstExonRes(tid);
        List<Integer> start_list_exon = tid_res.getStartList();
        return start_list_exon;
    }
    public static List<Integer> getExonEndList(String tid){
        OneEnstAllExonCoorClass tid_res = getEnstExonRes(tid);
        List<Integer> end_list_exon = tid_res.getEndList();
        return end_list_exon;
    }
}
