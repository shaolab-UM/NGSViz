package sqldbOperate;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.*;

/**
 * @author Benchen Ye
 * @create 2024-09--09:39
 */


public class ExecuteSQLFile {
    public static void main(String[] args) {
        String sqlFilePath = "/Users/bencheye/myProj/ngsPlot/NGSPlot2/database/interestElementGenomeInfoDB.sql";
        Connection connection = null;
        Statement statement = null;

        try {
            // 加载H2驱动程序
            Class.forName("org.h2.Driver");
            // 创建数据库连接
            connection = DriverManager.getConnection("jdbc:h2:/Users/bencheye/myProj/ngsPlot/NGSPlot2/database/interestElementGenomeInfoDB.sql", "root", "");
            statement = connection.createStatement();

            // 读取SQL文件
            BufferedReader reader = new BufferedReader(new FileReader(sqlFilePath));
            String line;
            while ((line = reader.readLine()) != null) {
                // 执行每一行的SQL命令
                statement.execute(line);
            }
            reader.close();
            System.out.println("SQL文件执行成功！");
        } catch (ClassNotFoundException | SQLException | IOException e) {
            e.printStackTrace();
        } finally {
            // 关闭Statement和Connection
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
