package com.NGSViz.utils;

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
        String csvFile = "/Users/benche/myProj/ngsPlot/NGSViz/sample_info.csv";
        System.out.println(readCsvConfigFile(csvFile));
    }

    public static List<String> readGeneList(String file_path) {
        List<String> gene_list = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(file_path))) {
            String line;
            while ((line = br.readLine()) != null) {
                gene_list.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return gene_list;
    }

    public static Map<String, Object> readTxtConfigFile(String file_path) {
        Map<String, Object> out_list = new HashMap<>();
        List<String> sample_list = new ArrayList<>();
        List<String> bam_list = new ArrayList<>();
        List<String> gene_list = new ArrayList<>();
        List<String> group_list = new ArrayList<>();
        List<String> title_list = new ArrayList<>();
        try {
            FileInputStream fis = new FileInputStream(file_path);
            InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
            BufferedReader reader = new BufferedReader(isr);
            String line;
            int i = 0;
            while ((line = reader.readLine())!= null) {
                sample_list.add(line.split(",")[0]);
                bam_list.add(line.split(",")[1]);
                gene_list.add(line.split(",")[2]);
                group_list.add(line.split(",")[3]);
                title_list.add(line.split(",")[4]);
                i++;
            }
            out_list.put("bam_list", bam_list);
            out_list.put("gene_list", gene_list);
            out_list.put("title_list", title_list);
            out_list.put("group_list", group_list);
            out_list.put("sample_list", sample_list);
            //System.out.println(out_list);
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return out_list;
    }
    public static Map<String, Object> readCsvConfigFile(String file_path) {

        Map<String, Object> out_list = new HashMap<>();
        List<String> sample_list = new ArrayList<>();
        List<String> bam_list = new ArrayList<>();
        List<String> gene_list = new ArrayList<>();
        List<String> group_list = new ArrayList<>();
        List<String> title_list = new ArrayList<>();
        try {
            FileInputStream fis = new FileInputStream(file_path);
            InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
            BufferedReader reader = new BufferedReader(isr);
            String line;
            int i = 0;
            while ((line = reader.readLine())!= null) {
                if (i==0){
                    i++;
                    continue;
                }
                sample_list.add(line.split(",")[0]);
                bam_list.add(line.split(",")[1]);
                gene_list.add(line.split(",")[2]);
                group_list.add(line.split(",")[3]);
                title_list.add(line.split(",")[4]);
                i++;
            }
            out_list.put("bam_list", bam_list);
            out_list.put("gene_list", gene_list);
            out_list.put("title_list", title_list);
            out_list.put("group_list", group_list);
            out_list.put("sample_list", sample_list);
            System.out.println(out_list);
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return out_list;
    }
}
