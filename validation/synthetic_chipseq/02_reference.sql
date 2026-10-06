CREATE TABLE defaultTbl (
  Species TEXT, CoordinateTblName TEXT, DB TEXT, Genome TEXT,
  Region TEXT, AnalysisType TEXT, Biotype TEXT, PointLab TEXT, FlankSize REAL
);
CREATE TABLE synthetic_RefSeq (
  chrom TEXT, start INTEGER, end INTEGER, width INTEGER,
  strand TEXT, type TEXT, biotype TEXT, gname TEXT, tid TEXT, DefaultChoose REAL
);
INSERT INTO defaultTbl VALUES
  ('Synthetic','synthetic_RefSeq','RefSeq','synthetic500','genebody','transcript','protein_coding','TSS-TES',0);
INSERT INTO synthetic_RefSeq VALUES
  ('chrSynthetic',1,500,500,'+','transcript','protein_coding','synthetic_full','NM_SYNTHETIC',1);
