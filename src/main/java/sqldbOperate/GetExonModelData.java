package sqldbOperate;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * @author Benchen Ye
 * @create 2024-11--21:54
 */
public class GetExonModelData extends DBAtribute{
    public static void main(String [] args){
        String species = "human";
        String db_name = "ensembl";
        String genome = "hg19";
        String refname = "hg19.ensembl.genebody.protein_coding";
        getExonModelData(genome, db_name, species);
    }
    public static void getExonModelData(String genome, String db_name, String species){
        String query_res = null;
        ResultSet resultSet = null;
        String region = "exonModel";
        String table_name = species + region;
        try {
            statement = initialDB();
            String query = String.format("SELECT tid,start,end,width FROM %s " +
                    "WHERE Genome = '%s' AND db = '%s'", table_name, genome, db_name);
            System.out.println("The query keyword is : " + query);
            resultSet = statement.executeQuery(query);
            while (resultSet.next()) {
                String query_tid = resultSet.getString("tid");
                int query_start = resultSet.getInt("start");
                int query_end = resultSet.getInt("end");
                int query_width = resultSet.getInt("width");
                System.out.println("The query tid is : " + query_tid);
                System.out.println("The query start is : " + query_start);
                System.out.println("The query end is : " + query_end);
                System.out.println("The query width is : " + query_width);
            }
        } catch (SQLException e) {
            System.out.println("Database read data error: " + e.getMessage());
        }
        exitDB();
        //return query_res;
    }
}
