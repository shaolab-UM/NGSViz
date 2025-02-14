package sqldbOperate;

import java.util.List;
import java.util.Map;

/**
 * @author Benchen Ye
 * @create 2025-02--20:34
 */
public class main {
    public static void main(String[] args) {
        String genome = "hg19";
        String DB_tpye = "RefSeq";
        String biotype = "protein_coding";
        String analysis_type = "transcript";
        // get the coordinate database will be used
        //refname = GetRefDB.getDBTableName(genome, DB_tpye);
        // get the species name
        String species = GetRefDB.getSpeciesName(genome, DB_tpye);
        // get the table name of genome coordinate information
        String tbl_name = GetRefDB.getTblName(genome, DB_tpye);
        // get the whole coordinate
        QueryWholeRegionCoordinateCP.queryGenomeCoorDatabaseRecord(tbl_name, biotype, analysis_type);
        Map<String, List<Transcript>> gene_map = QueryWholeRegionCoordinate.gene_map;
        System.out.println(gene_map.get("CDKN2C"));
    }
}
