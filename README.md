# NGSVir

## Introduction

## Install



## Quick Start

You can filter the transcripts of interest based on Gene name or NCBI's Ref seq ID.

### Input file

parameter `-I` is provided to input the **absolute path** for preparing to analyze bam files.

parameter (Optional) `-X` is provided to input the **absolute path** for preparing to analyze  the gene set of interest . The default value is `all`, indicating the selection of all genes in the database.

parameter (Optional) `-T` is provided to input the **analysis title**.

#### 

## Database

### Database structure

The database contains a `defaultTbl` table and multiple installed database tables CoordinateTblName, formatted as: **{genome_version}_RefSeq**, such as: `hg19_RefSeq`.

The table `defaultTbl` stores information about the installed database and some default parameters that the program can use. Specifically:

| Species      | CoordinateTblName | DB     | Genome | Region   | AnalysisType | Biotype        | PointLab       | FlankSize |
| ------------ | ----------------- | ------ | ------ | -------- | ------------ | -------------- | -------------- | --------- |
| Homo_sapiens | hg19_RefSeq       | RefSeq | hg19   | exon     | exon         | protein_coding | Acceptor-Donor | 500       |
| Homo_sapiens | hg19_RefSeq       | RefSeq | hg19   | genebody | transcript   | protein_coding | TES            | 2000      |
| Homo_sapiens | hg19_RefSeq       | RefSeq | hg19   | tss      | transcript   | protein_coding | TSS            | 2000      |
| Homo_sapiens | hg19_RefSeq       | RefSeq | hg19   | tes      | transcript   | protein_coding | TSS-TES        | 2000      |

The `CoordinateTblName` contains the program to obtain the interested genomic elements and their coordinate information. Among them, `DefaultChoose` is used for the default analysis case where a gene contains multiple transcripts, and it defaults to the first longest transcript.

| chrom | start     | end       | width | strand | type       | biotype        | gname | tid          | DefaultChoose |
| ----- | --------- | --------- | ----- | ------ | ---------- | -------------- | ----- | ------------ | ------------- |
| chr1  | 249200434 | 249213345 | 12912 | +      | transcript | protein_coding | PGBD2 | NM_001017434 | 1             |
| chr1  | 249200434 | 249200541 | 108   | +      | exon       | protein_coding | PGBD2 | NM_001017434 | 0             |
| chr1  | 249208015 | 249208078 | 64    | +      | exon       | protein_coding | PGBD2 | NM_001017434 | 0             |
| chr1  | 249200434 | 249213345 | 12912 | +      | transcript | protein_coding | PGBD2 | NM_170725    | 0             |

### Reference database

This program provides a reference database of multiple species and multiple versions. Users can download the corresponding database from the provided online disk according to their research needs and import it into the program's dependent database.

### Modify the build-in batabase path

The default built-in database, `genomeCoordinate.db`, is located in the program's `database` directory. Its location is specified by the system settings file, `NGSViz_setting.json`, found in the system setting path relative to the program directory. However, due to containerization or other reasons, this database might be in read-only mode, which could prevent users from adding new databases using the `merge` method. The program allows users to create a new `NGSViz_setting.json` file at any location. When running the program, they can specify the path to their custom system configuration file using the `-CP` parameter or through the Rshiny module. Within this file, they can modify the `db_path` parameter to point to the path of their new reference database.

### 🧬 Chromosome Naming Compatibility When Processing BAM Files

When processing BAM files, it's important to ensure consistency between the reference genome used for alignment and the corresponding GTF annotation file. Different genome sources may use different chromosome naming conventions, which can lead to mismatches during downstream analysis.

For example:

- **Ensembl** reference genomes and GTF files typically use chromosome names like `1`, `2`, `X` (without the `chr`prefix).
- **UCSC** and **GENCODE** use names like `chr1`, `chr2`, `chrX` (with the `chr` prefix).
- **NCBI RefSeq** uses accession-based identifiers such as `NC_000001.11`.

Our tool is built using **UCSC GTF annotations**, which follow the `chr`-prefixed naming convention. Therefore, it expects chromosome names in the format `chr1`, `chr2`, etc. GTF files following UCSC-style naming are fully supported and handled natively. Our tool automatically detects and adapts to BAM files aligned using Ensembl-style reference genomes (without the chr prefix). No manual intervention or configuration is required from the user.

To ensure compatibility:

- If your BAM file was aligned using a reference genome without the `chr` prefix (e.g., from Ensembl), you may need to adjust the chromosome names in the BAM or GTF file accordingly.
- Our tool **automatically handles** GTF files that follow the UCSC-style naming convention.

> ⚠️ **Note:**  We do **not support automatic conversion** of NCBI-style accession identifiers (e.g., `NC_000001.11`). If you're using RefSeq-based annotations, please convert chromosome names to UCSC format before proceeding.

### Merge database

By specifying the database path to be imported using the `-DB` parameter, a database can be merged into the database that the program depends on. The database to be imported can be downloaded from our cloud storage. The database of this program is specified in the path of the ngsViz system configuration.

```shell
inport_db_path=/Users/bencheye/myProj/ngsPlot/DB/DB/Homo_sapiens/NGSViz_hg19_RefSeq.db
java -jar ngsViz_1.0.jar -DB $inport_db_path
```



### Generate the json file of default plot parameters

When NGSViz.jar runs, it will generate a JSON file `NGSViz_plotSetting.json` with default plotting parameters in the output path. Users can directly use our optimized parameters for plotting, or they can modify the parameters in the configuration file themselves to personalize the graphics, or read the parameters into RShiny and then modify them there to draw the graphics.



## Feature

We integrate commonly used variables together to facilitate code maintenance and invocation, reducing redundancy.

## Test

```java
args = new String[]{
                "-G", "hg19",
                "-R", "genebody",
                //"-T", "GeneName",
                //"-E", "/Users/bencheye/myProj/ngsPlot/Data/genelist.txt",
                //"-R", "tss",
                "-D", "RefSeq",
                //"-R", "tes",
                "-I", "/Users/bencheye/myProj/ngsPlot/Data/hesc.H3k27me3.1M.bam",
                //"-C", "/Users/bencheye/myProj/ngsPlot/Data/hesc.RNAseq.1M.bam",
                //"-C", "/Users/bencheye/myProj/ngsPlot/Data/input_para.txt",
                "-O", "/Users/bencheye/myProj/ngsPlot/Output/Result",
                //"-F", "protein_coding",
                "-A", "transcript",
                //"-M", "bin",
                //"-F", "2000"
        };
```



## Tutorial

Each run creates a run json configuration file in the output path, storing the parameters of the program execution.

conda install conda-forge::openjdk

## Parameter 



| Parameter Name |  Variable Name  |  Default Value   |           Describtion            |
| :------------: | :-------------: | :--------------: | :------------------------------: |
|      -DB       |  input_db_path  |        -         |  Import database path to merge   |
|      -CP       | sys_config_path |        -         |                                  |
|       -G       |     genome      |        -         |  the reference genome database   |
|       -R       |   region_type   |        -         |   the region type for analysis   |
|       -I       |   input_file    |        -         |         Input file path          |
|       -O       |   output_path   |        -         |           output path            |
|       -D       |     DB_tpye     |      RefSeq      |   Reference sequence database    |
|       -X       |    **gene**s    |     **all**      |      gene list for analysis      |
|       -T       |    **title**    | Average Coverage |          analysis title          |
|       -A       |  analysis_type  |    transcript    |          analysis type           |
|       -B       |     biotype     |  protein_coding  |            gene type             |
|       -F       |  flank_region   |       2000       |       Flanking region size       |
|       -N       |  flank_factor   |       0.0        |       Flanking size factor       |
|                |                 |                  |                                  |
|                |                 |                  |                                  |
|       -M       |  scaler_method  |       bin        | method to scale the query region |
|      -DP       | num_datapoints  |       100        |   number of data point to plot   |
|                |                 |                  |                                  |
|      -MQ       |    min_mapq     |        20        |     threshold to filter read     |
|      -FL       |    frag_len     |       150        |         Fragment length          |
|      -SS       |   strand_spec   |       both       |     read strand in bam file      |
|       -P       |    core_num     |                  |                                  |

## Database



|  Species  |                         | Genome Version |
| :-------: | :---------------------: | :------------: |
|   Human   |      Homo_sapiens       |      hg38      |
|   Human   |      Homo_sapiens       |   Versiont2t   |
|   Human   |      Homo_sapiens       |      hg19      |
|   Mouse   |      Mus_musculus       |      mm39      |
|   Mouse   |      Mus_musculus       |      mm10      |
|   Mouse   |      Mus_musculus       |      mm9       |
| Zebrafish |       Danio_rerio       |    danRer11    |
| Zebrafish |       Danio_rerio       |    danRer10    |
| Zebrafish |       Danio_rerio       |    danRer7     |
| Fruit fly | Drosophila_melanogaster |      Dm6       |
| Fruit fly | Drosophila_melanogaster |      dm3       |
|    Cat    |       Felis_catus       |    felCat9     |
|    Cat    |       Felis_catus       |    felCat8     |
|    Cat    |       Felis_catus       |    felCat5     |
|    Dog    |    Canis_familiaris     |                |
|           |                         |                |
|           |                         |                |
|           |                         |                |
|           |                         |                |
|           |                         |                |
|           |                         |                |
|           |                         |                |
|           |                         |                |
|           |                         |                |
|           |                         |                |
|           |                         |                |
|           |                         |                |
|           |                         |                |
|           |                         |                |



NGSViz运行

