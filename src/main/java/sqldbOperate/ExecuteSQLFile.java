package sqldbOperate;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.*;

/**
 * @author Benchen Ye
 * @create 2024-09--09:39
 */

// H2 link SQL independent on the local SQL sever
// there are some bugs needed to fix


public class ExecuteSQLFile {
    public static void main(String[] args) {
        String sqlFilePath = "/Users/bencheye/myProj/ngsPlot/NGSPlot2/database/interestElementGenomeInfoDB.sql";
        Connection connection = null;
        Statement statement = null;

        try {
            // load H2 drive
            Class.forName("org.h2.Driver");
            // create a db link
            connection = DriverManager.getConnection("jdbc:h2:/Users/bencheye/myProj/ngsPlot/NGSPlot2/database/interestElementGenomeInfoDB.sql", "root", "");
            statement = connection.createStatement();

            // read SQL file
            BufferedReader reader = new BufferedReader(new FileReader(sqlFilePath));
            String line;
            while ((line = reader.readLine()) != null) {
                // run each line SQL command
                statement.execute(line);
            }
            reader.close();
            System.out.println("SQL file running finished！");
        } catch (ClassNotFoundException | SQLException | IOException e) {
            e.printStackTrace();
        } finally {
            // stop Statement adn Connection
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
