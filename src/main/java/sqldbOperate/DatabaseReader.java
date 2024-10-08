package sqldbOperate;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Benchen Ye
 * @create 2024-09--15:45
 */

// select the items from database table
public class DatabaseReader {
    private static final String URL = "jdbc:mysql://localhost:3306/interestElementGenomeInfoDB";
    private static final String USER = "root";
    private static final String PASSWORD = "";


    public static void main(String[] args) {
        String query_keyword = "hg19.ensembl.genebody.protein_coding";

        List<Map<String, Object>> results = new ArrayList<>();
        String query = "SELECT *  FROM elementgenomecoordinate WHERE refname = ?";
        try (Connection connection = getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, query_keyword);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    Map<String, Object> record = new HashMap<>();
                    // get the items from database table with getString/getInt
                    record.put("chrom", resultSet.getString("chrom"));
                    record.put("start", resultSet.getInt("start"));
                    record.put("end", resultSet.getInt("end"));
                    results.add(record);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        results.parallelStream().forEach(DatabaseReader::processRecord);
    }

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("Driver loaded!");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    private static void processRecord(Map<String, Object> record) {
        System.out.println("Processing record: " + record);
    }
}