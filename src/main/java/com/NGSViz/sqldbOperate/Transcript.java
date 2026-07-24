package com.NGSViz.sqldbOperate;

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
    private int index;
    // Static counter, initialized to 0
    private static int indexCounter = 0;

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
        this.index = indexCounter++;
    }

    public static void resetIndexCounter() {
        indexCounter = 0;
    }

    public String getRecordName(){
        return record_name;
    }

    public String getGeneName() {
        return gene_name;
    }
    public int getIndex() {
        return index;
    }

    public int getStartPos() {
        return start_pos;
    }

    public int getEndPos() {
        return end_pos;
    }

    public String getChrLab(){
        return chr_lab;
    }

    public String getNoChrLab(){
        return no_chr_lab;
    }

    public String getQueryStrand() {
        return query_strand;
    }

    @Override
    public String toString() {
        return "Gene{" +
                "index=" + index +
                ", name='" + record_name + '\'' +
                ", chromosome='" + chr_lab + '\'' +
                ", strand='" + query_strand + '\'' +
                ", start=" + start_pos +
                ", end=" + end_pos +
                //", width=" + width +
                '}';
    }

}
