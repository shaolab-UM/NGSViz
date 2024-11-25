import configSet.InputParameterAttributes;
import configSet.ProcessInputParameters;
import coverageCalculator.LargeIntervalMain;
import coverageCalculator.MainCalculator;
import htsjdk.samtools.Chunk;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Benchen Ye
 * @create 2024-11--11:35
 */
public class test extends InputParameterAttributes {
    public static void main(String[] args) {
        String input = "A1BG:ENST00000263100";
        // 使用冒号进行拆分
        String[] parts = input.split(":");
        // 获取后面的字符
        String result = parts.length > 1 ? parts[1] : "";
        // 打印结果
        System.out.println("后面的字符: " + result);
    }

}
