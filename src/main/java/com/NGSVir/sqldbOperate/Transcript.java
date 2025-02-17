package com.NGSVir.sqldbOperate;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Benchen Ye
 * @create 2025-02--19:56
 */
public class Transcript {
    private String gene_name;
    private String transcript_id;
    private int start_pos;
    private int end_pos;
    private String record_name;
    private String query_strand;
    private String chr_lab;
    private String no_chr_lab;
    private static Map<String, Object> record = new HashMap<>();

    public Transcript(String record_name,
                      String gene_name,
                      String transcript_id,
                      String chr_lab,
                      String no_chr_lab,
                      String query_strand,
                      int start_pos,
                      int end_pos
    ) {
        this.gene_name = gene_name;
        this.transcript_id = transcript_id;
        this.start_pos = start_pos;
        this.end_pos = end_pos;
        this.record_name = record_name;
        this.query_strand = query_strand;
        this.chr_lab = chr_lab;
        this.no_chr_lab = no_chr_lab;
    }

    public String getGeneName() {
        return gene_name;
    }

    public Map<String, Object> getQueryCoord(){
        record.put("chrom", chr_lab);
        record.put("nochr_name", no_chr_lab);
        record.put("gene_name", gene_name);
        record.put("start", start_pos);
        record.put("end", end_pos);
        record.put("strand", query_strand);
        record.put("record_name", record_name);
        return record;
    }

}
