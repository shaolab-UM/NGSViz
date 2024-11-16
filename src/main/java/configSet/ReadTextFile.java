package configSet;

import java.io.*;
import java.util.*;

/**
 * @author Benchen Ye
 * @create 2024-11--20:35
 * @function read the input file
 * @input `file_path` the path of input file
 * @output Map<String, Object> to save information for bam_file, gene_list, and title
 */
public class ReadTextFile {
    public static void main(String[] args) {
        String file_path = "/Users/bencheye/myProj/ngsPlot/Data/input_para.txt";
        readTxtConfigFile(file_path);
    }
    public static Map<String, Object> readTxtConfigFile(String file_path) {
        Map<String, Object> out_list = new HashMap<>();
        List<String> bam_list = new ArrayList<>();
        List<String> gene_list = new ArrayList<>();
        List<String> title_list = new ArrayList<>();
        try {
            FileInputStream fis = new FileInputStream(file_path);
            InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
            BufferedReader reader = new BufferedReader(isr);
            String line;
            int i = 0;
            while ((line = reader.readLine())!= null) {
                bam_list.add(line.split("\t")[0]);
                gene_list.add(line.split("\t")[1]);
                title_list.add(line.split("\t")[2]);
                i++;
            }
            out_list.put("bam_list", bam_list);
            out_list.put("gene_list", gene_list);
            out_list.put("title_list", title_list);
            //System.out.println(out_list);
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return out_list;
    }
    public static Map<String, Object> readCsvConfigFile(String file_path) {
        Map<String, Object> out_list = new HashMap<>();
        List<String> bam_list = new ArrayList<>();
        List<String> gene_list = new ArrayList<>();
        List<String> title_list = new ArrayList<>();
        try {
            FileInputStream fis = new FileInputStream(file_path);
            InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
            BufferedReader reader = new BufferedReader(isr);
            String line;
            int i = 0;
            while ((line = reader.readLine())!= null) {
                bam_list.add(line.split(",")[0]);
                gene_list.add(line.split(",")[1]);
                title_list.add(line.split(",")[2]);
                i++;
            }
            out_list.put("bam_list", bam_list);
            out_list.put("gene_list", gene_list);
            out_list.put("title_list", title_list);
            System.out.println(out_list);
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return out_list;
    }
}
