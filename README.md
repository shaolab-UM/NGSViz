# NGSVir User Guide

# Table of Contents

[TOC]

---

## 📘 Introduction

**NGSViz** is an analysis and visualization tool for high-throughput sequencing (NGS) data, supporting input files in BAM format, integrating an efficient Java computing engine, flexible R visualization scripts, and an interactive R Shiny interface, providing a variety of commonly used built-in genomic functional element databases, suitable for various scenarios such as genomic coverage and enrichment analysis.

###  Typical Analysis Workflow

1. **Prepare the Reference Database**

   - Build the reference database using the script `lib/buildRefDB.R`, or download a pre-built version from the provided link. In addition, users can build their own reference databases by following the format and rules provided by this program.
   - Ensure the database path is correctly specified in the system configuration file (`NGSViz_setting.json`) or via the `-DB` parameter.

2. **Prepare BAM files to analyse.** 

   > ⚠️ **Note:** BAM files require a corresponding bam index file (bai) to be provided. If a BAM index is not available, it must be generated using SAMtools.

3. **Launch the RShiny Interface or Run via Command Line**

   - Start the program either through the RShiny interface or by executing the command-line tool with appropriate parameters.

   ```
   # GUI mode
   ## run Rshiny
   R -e "shiny::runApp('ngsViz-Shiny.R', host='0.0.0.0', port=3838)"
   # CLI mode
   ## 1. Run java calculation module
   java -jar NGSViz-1.3.jar -G <genome_version> -R <region_type> -I <input_data> -O <output> -T <analysisTitle>
   ## 1. Run visualization moddule
   Rscript lib/ngsVizPlotMain.R -O <output>
   ```

4. **Set Required Parameters**

   - Specify the reference genome version (`-G`), region type (`-R`), analysis title (`-T`), input file (`-I`), and output path (`-O`).
   - Optionally, define number of CPU cores to use (`-P`), biotype (`-B`), and other advanced parameters.

5. **Run the Calculation Module**

   - Execute the analysis by clicking **Start Analysis** in the RShiny interface or running the command-line tool.
   - The program will process the input BAM file and generate a coverage matrix and JSON configuration file used for downstream visualization tasks.

6. **Visualize the Results**

   For result visualization, there are two options: either use the three individual visualization modules within R shiny, or directly generate the visualizations results using the `ngsVizPlotMain.R` wrapper script. Users can fine-tune the output image's visualization effects by modifying the default parameters in the configuration file.
   
   **visualization modules within R shiny**:
   
   - **SingleCoveragePlot** for individual samples.
   - **MultiSamplePlot** for average and integrated views.
   - **QCPlot** for PCA and correlation analysis across samples.

---

## ⚙️Installation

### GitHub installation

#### 1. Clone code repository

```bash
git clone git@github.com:shaolab-UM/NGSViz.git
```

#### 2. Install dependency environment

- The committed `environment.yml` is a reproducible full-environment lock for **Linux x86_64**. Create it only on that platform with `conda env create -f environment.yml`.
- For automatic Java setup on macOS arm64, macOS x86_64, or Linux, use the portable Java-only environment: `conda env create -f environment-java.yml`.
  - Verify it with `conda run -n ngsViz-java java -version`; Java 17 or above is required.
  - The portable file intentionally leaves platform builds to Conda and contains no `linux-64` packages, sysroot, or machine-specific prefix.
- On platforms other than Linux x86_64, use the container installation below for the complete locked R and system dependency stack, or install R 4.0+ and its dependencies separately.


#### 4. Other Dependencies

- It is recommended to install commonly used bioinformatics tools such as samtools (if needed to process BAM files).

#### 5. Set software global configuration

- To modify the global configuration file `NGSViz_setting.json`, update the values of `tool_path`, `db_path`, and `java_path`, which correspond to the tool's installation path, the path to the required database, and the path to the Java environment, respectively.

  If you do not have permission to edit the original configuration file, you can create a new JSON configuration file with the following content. When running the computation module of ngsViz, use the `-CP` parameter to specify the path to your newly created configuration file.

  ```json
  {
      "versionNum": {
          "version": "1.2"
      },
      "toolParas": {
          "tool_path": "/path/to/NGSViz",
          "db_path": "/path/to/NGSViz/database/genomeCoordinate.db",
          "tool_name": "NGSViz-1.3.jar",
          "java_path": "/usr/bin/java" 
      }
  }
  ```

---

### **Install and run using a container**

#### Run with docker

```shell
# pull the docker image of ngsViz
docker pull ngsviz/ngsviz:1.2
# build the docker container
docker run \
--name ngsViz \
-p 3838:3838 \
-v $localDataPath:/data \  
-dit ngsviz/ngsviz:1.2
# Start container
docker start ngsViz 
# Enter the container
docker attach ngsViz 
```

`-v`: Mount the local `localDataPath` to the container's `/data` to facilitate data communication between the local system and the container.

`-p`: Connect the local machine's port 3838 to the container's port 3838 to enable access to the RShiny interface in the container via http://localhost:3838.

#### Run with singularity

You can also build a **Singularity image** based on our **Docker image** for use with Singularity. Here's how:

```singularity exec your-image.sif java -version
singularity build ngsviz.sif docker://ngsviz/ngsviz:1.2
singularity exec ngsviz.sif java -jar ngsViz-10.jar -G hg19 -R tss -I $input_data -O $output -T analysisTitle
```

#### Run with Apptainer

If the system lacks a container runtime environment, it is recommended to use Conda to install `Apptainer`, a container platform designed for secure and portable high-performance computing. Apptainer is designed to enable unprivileged users (without root privileges) to run containers securely and efficiently. Below, we provide some simple examples. For more advanced usage, please refer to the official documentation. 

```shell
# install the Apptainer
conda install conda-forge::apptainer
# 1.1 Pull Docker container image
apptainer pull docker://ngsviz/ngsviz:1.2
# 1.2 Run Docker container
apptainer run docker://ngsviz/ngsviz:1.2
# 2. Run Singularity container
apptainer run ngsviz.sif java -jar ngsViz-1.3.jar -G hg19 -R tss -I $input_data -O $output -T analysisTitle
```

---

## 🧩Main modules and script description

### Database

#### Chromosome Naming Compatibility When Processing BAM Files

When processing BAM files, it's important to ensure consistency between the reference genome used for alignment and the corresponding GTF annotation file. Different genome sources may use different chromosome naming conventions, which can lead to mismatches during downstream analysis.

For example:

- **Ensembl** reference genomes and GTF files typically use chromosome names like `1`, `2`, `X` (without the `chr`prefix).
- **UCSC** and **GENCODE** use names like `chr1`, `chr2`, `chrX` (with the `chr` prefix).
- **NCBI RefSeq** uses accession-based identifiers such as `NC_000001.11`.

Our tool is built using **UCSC GTF annotations**, which follow the `chr`-prefixed naming convention. Therefore, it expects chromosome names in the format `chr1`, `chr2`, etc. GTF files following UCSC-style naming are fully supported and handled natively. Our tool automatically detects and adapts to BAM files aligned using Ensembl-style reference genomes (without the chr prefix). No manual intervention or configuration is required from the user.

> ⚠️ **Note:**  We do **not support automatic conversion** of NCBI-style accession identifiers (e.g., `NC_000001.11`). If you're using RefSeq-based annotations, please convert chromosome names to UCSC format before proceeding.

#### Database structure and Functionality

The database includes a table named `defaultTbl` and multiple installed database tables referred to as CoordinateTblName. These tables follow the naming format **{Genome}_{DB}**, such as: `hg19_RefSeq`.

- **`defaultTbl`**
  This table stores metadata about the installed databases and default parameters used by the program. It helps the application determine which genome annotations are available and provides default settings for analysis.

| Column | Item              | Description                                                  |
| ------ | ----------------- | ------------------------------------------------------------ |
| 1      | Species           | Species name (e.g., *Homo sapiens*, *Mus musculus*)          |
| 2      | CoordinateTblName | Table name containing genomic feature coordinates (e.g., hg19_RefSeq) |
| 3      | DB                | Source of annotation database (e.g., RefSeq)                 |
| 4      | Genome            | Reference genome version (e.g., hg38, mm10)                  |
| 5      | Region            | Type of genomic region (e.g., gene body, tss, tes, exon)     |
| 6      | AnalysisType      | Type of analysis performed (e.g., exon or transcript)        |
| 7      | Biotype           | Gene classification (e.g., protein-coding, lncRNA, pseudogene) |
| 8      | PointLab          | Labeled point of interest (e.g., TSS, TES)                   |
| 9      | FlankSize         | Flanking region size added upstream and downstream of the query region |

- **`CoordinateTblName`**
  These tables contain genomic elements of interest along with their coordinate information. They are used by the program to retrieve relevant genomic features for analysis. `DefaultChooseSpecifies` the default representative transcript, chosen as the longest when multiple transcripts are present.`Biotype` indicates the gene type, such as protein-coding or non-coding RNA. `type` specifies whether the entry represents a transcript or an exon.

| Column | Item          |              Description               |
| ------ | ------------- | :------------------------------------: |
| 1      | chrom         |               Chromosome               |
| 2      | start         |             Start Position             |
| 3      | end           |              End Position              |
| 4      | width         |                 width                  |
| 5      | strand        |     Strand Direction (`+` or `-`)      |
| 6      | type          |           Transcript or exon           |
| 7      | biotype       |          Gene classification           |
| 8      | gname         |               Gene Name                |
| 9      | tid           |             Transcript ID              |
| 10     | DefaultChoose | Longest transcript selected by default |

#### Reference database

This program offers a reference database that supports multiple species and genome versions. Users can download the appropriate database from the provided online repository based on their research needs, and import it into the program’s dependent database.

You can access and download the pre-built genomic functional element databases from the following link:
[**Download Link – Pre-built Database of Genomic Functional Elements**](https://drive.google.com/drive/folders/1h3tORc5PiZ_TTbIH9cH03O1wyqjhNMDz?usp=sharing).

Pre-built database of genomic functional elements

| Organism  |     Scientific Name     | Genome Version | Annotation Source        |
| :-------: | :---------------------: | :------------: | ------------------------ |
|   Human   |      Homo_sapiens       | [hg38](https://drive.google.com/uc?export=download&id=1XprZE54eZ14FBGrvIajy91_O9ZOfwJSW) | UCSC (RefSeq annotation) |
|   Human   |      Homo_sapiens       | [hg19](https://drive.google.com/uc?export=download&id=1XTp7i780krY87Uc1JDtVrz-Y5oimuitZ) | UCSC (RefSeq annotation) |
|   Mouse   |      Mus_musculus       | [mm39](https://drive.google.com/uc?export=download&id=1lsbnW18O_MsQrGr2mRq-_cBrf2sHeGBw) | UCSC (RefSeq annotation) |
|   Mouse   |      Mus_musculus       | [mm10](https://drive.google.com/uc?export=download&id=1fzTrVOWF2V-VPDODo6AS30kzAPNgHyt_) | UCSC (RefSeq annotation) |
|   Mouse   |      Mus_musculus       | [mm9](https://drive.google.com/uc?export=download&id=1vjIk74g29LN7a7P8nwVyJMwtRhXR-b4r) | UCSC (RefSeq annotation) |
| Zebrafish |       Danio_rerio       | [danRer11](https://drive.google.com/uc?export=download&id=1M3sxoVc2pAqAisTjVUsjnDaKBKYJHUUI) | UCSC (RefSeq annotation) |
| Zebrafish |       Danio_rerio       | [danRer10](https://drive.google.com/uc?export=download&id=1gWaMnpPAMKYN4kzvaaw-oW3w1dnKy4_0) | UCSC (RefSeq annotation) |
| Zebrafish |       Danio_rerio       | [danRer7](https://drive.google.com/uc?export=download&id=12MDJu47MGpoHqqM0o5w2VRouc75Y42_0) | UCSC (RefSeq annotation) |
| Fruit fly | Drosophila_melanogaster | [Dm6](https://drive.google.com/uc?export=download&id=1k5tiDT6bwRj1DE6uUsLKB4K1UbtcGOH4) | UCSC (RefSeq annotation) |
| Fruit fly | Drosophila_melanogaster | [dm3](https://drive.google.com/uc?export=download&id=1Y1V5lNyegP3DZYLUKP-GwijrbxHdOxOb) | UCSC (RefSeq annotation) |
|    Cat    |       Felis_catus       | [felCat9](https://drive.google.com/uc?export=download&id=1mTiasy8GgfTcaRPZ6EP5SqiWoOCDKSpT) | UCSC (RefSeq annotation) |
|    Cat    |       Felis_catus       | [felCat8](https://drive.google.com/uc?export=download&id=12gMeK7iAM6KP5S1LRjlzi7gMRMy6B56h) | UCSC (RefSeq annotation) |
|    Cat    |       Felis_catus       | [felCat5](https://drive.google.com/uc?export=download&id=1TZrdhjFFvFfZVlnHiCDPrNrpPrOjGUMS) | UCSC (RefSeq annotation) |
|    Dog    |    Canis_familiaris     | [canFam6](https://drive.google.com/uc?export=download&id=1JbB6udpnax4ZHjwUuaKZwRq0NNnWVRoG) | UCSC (RefSeq annotation) |

#### Modify the build-in batabase path

The default built-in database, `genomeCoordinate.db`, is located in the program’s `database`directory. Its location is defined in the system configuration file `NGSViz_setting.json`, which resides in the system settings path relative to the program directory.

However, in environments such as containers, this database may be mounted in read-only mode, preventing users from adding new databases using the `merge` method. To address this, the program allows users to create a custom `NGSViz_setting.json` file at any location. When launching the program, users can specify the path to this custom configuration file using the `-CP` parameter or through the RShiny interface. Within this file, the `db_path` parameter can be modified to point to the desired reference database location.

#### Merge database

To merge a new database into the program’s existing reference database, use the `-DB` parameter to specify the path of the database to be imported. The database can either be downloaded from our cloud storage or built manually using the provided script `lib/buildRefDB.R`.

> **⚠️Note:** All pre-built databases can be used independently. To activate a downloaded database, simply specify its path in the system configuration settings `NGSViz_setting.json`. Once configured, the program will recognize and utilize the selected database without requiring additional setup.

```shell
inport_db_path=$YOUR_PATH/NGSViz_hg19_RefSeq.db
java -jar NGSViz-1.3.jar -DB $inport_db_path
```

#### Build a reference database (optional)

If the pre-built databases do not include the species or genome version you require, you can build a custom reference database using the script `lib/buildRefDB.R`. This script constructs the database based on a genomic annotation file in GTF format. The parameter  `annotatedNCRNA` is used to specify whether to further annotate ncRNA using the `biomaRt` package, annotating it into more specific types such as lncRNA, etc. `F` indicates that no annotation will be performed.

> ⚠️**Note:** It is important to note that the name of the GTF file in the specified path must be in the format of the genome version, such as genome_version.annotation_type.gtf. For example, `hg38.ncbiRefSeq.gtf`.
>
> ------
>
> ⚠️**Note:** If you're using an Ensembl GTF file, you'll need to **add the "chr" prefix** to the chromosome names.

**Usage:**

```bash
Rscript lib/buildRefDB.R <gtf_file> <DB_name> <species> <annotatedNCRNA>
```

**Parameter Description**

|   Parameter    |                    Description                     |       Example       |
| :------------: | :------------------------------------------------: | :-----------------: |
|    gtf_file    |         Specifies the path to the GTF file         | hg38.ncbiRefSeq.gtf |
|    DB_name     |             The name of the database.              |       RefSeq        |
|    species     |              The name of the species.              |    Homo_sapiens     |
| annotatedNCRNA | Indicates whether to refine annotations for ncRNA. |          F          |

#### **Custom Functional Element Database**

In addition to the built-in databases, the program also supports user-defined functional element databases. Users can construct their own databases following the required format and specify the path using the `-BD` parameter when running the program.

- The custom database must be **tab-delimited** and **must not contain a header row**.
- Each row should represent a functional genomic element.
- The database must not contain redundant entries, as the program will read and process all records sequentially.
- Minimum required columns are **1–3** (`Chromosome`, `Start`, `End`). If optional columns are missing, NGSViz will assume **`+` strand** and auto-generate stable element IDs from coordinates.

| Column |          Description          |
| :----: | :---------------------------: |
|   1    |          Chromosome           |
|   2    |        Start Position         |
|   3    |         End Position          |
|   4    | Strand Direction (`+` or `-`) |
|   5    |           Gene Name           |
|   6    |         Transcript ID         |

---

#### **Intergenic Region Annotation** 

NGSViz provides built-in **intergenic region annotations** for both **human and mouse**, which are included in the database for convenient and direct use. Users can specify the desired intergenic annotation file using the **`-BD`** parameter when running the program. These annotations are fully formatted and require no additional preprocessing, enabling straightforward exploration and visualization of genomic features within intergenic regions.

---

### Computational Module Based on Java

#### Feature

- No environment dependencies
- Multithreading concurrency, supports large-scale data
- Efficiently process BAM files, calculate genomic coverage.

#### Usage

```bash
java -jar NGSViz-1.3.jar [parameter]
```

#### Native AI-Friendly Request Interface (Planned)

AI workflows pass computation parameters through one single-sample JSON request file. They do not expand the JSON fields into legacy CLI flags.

```bash
java -jar NGSViz-1.3.jar validate-compute --request compute_request.json
java -jar NGSViz-1.3.jar run-compute --request compute_request.json
```

Legacy CLI flags and `--request` are mutually exclusive inputs. Both are normalized internally to the same `ComputeRequest`, validated through the same boundary, and passed to the same `MainCalculator`.

```text
legacy CLI flags ──────┐
                      ├─> ComputeRequest ─> validate ─> MainCalculator
compute_request.json ─┘
```

#### Parameters

| Parameter Name |  Variable Name  | Default Value  |                Describtion                 |
| :------------: | :-------------: | :------------: | :----------------------------------------: |
|       -H       |        -        |       -        |        Print all command usage help        |
|       -V       |        -        |       -        |       Print the current ngsViz version      |
|       -J       | ai_config_path  |       -        | Deprecated placeholder; does not load AI configuration |
|       -G       |     genome      |       -        |         Reference genome database          |
|       -R       |   region_type   |       -        |          Region type for analysis          |
|       -T       |      title      |       -        |               Analysis title               |
|       -I       |   input_file    |       -        |             Path to input file             |
|       -O       |   output_path   |       -        |          Path to output directory          |
|      -DB       |  input_db_path  |       -        |        Path to database for merging        |
|      -CP       | sys_config_path |       -        |     Path to system configuration file      |
|      -BD       |   bedDB_path    |       -        | Path to custom functional element database |
|       -X       |      genes      |      all       |         List of genes for analysis         |
|      -BS       |   batch_size    |      500       |        batch size for gene parallel        |
|       -B       |     biotype     | protein_coding |                Gene biotype                |
|       -F       |  flank_region   |      2000      |        Flanking region size (in bp)        |
|       -N       |  flank_factor   |      0.0       |        Flanking size scaling factor        |
|      -DP       | num_datapoints  |      100       |       Number of data points to plot        |
|      -MQ       |    min_mapq     |       20       |     Minimum mapping quality threshold      |
|      -FL       |    frag_len     |      150       |              Fragment length               |
|       -P       |    core_num     |       1        |         Number of CPU cores to use         |
|       -S       |   scale_ratio   |               null|     Scaling ratio for signal intensity     |
|      -NF       |   new_forder    |     false      |           Create Results Folder            |
|      -BM       |   bin_method    |      mean      |                 Bin method                 |
|      -CM       |   Centermode    |     false      |          Whether plot with center          |

#### Command-Line Parameters Description

- **`-H`**: Prints the complete Java command-line usage help and exits without starting an analysis.
- **`-V`**: Prints the current version (`ngsViz 1.3`) and exits without starting an analysis.
- **`-J`**: Deprecated placeholder retained in the current parser. It does not load an AI configuration and must not be used by the AI-friendly workflow.
- **`-G`**: (**Required parameters**) Indicates the version of the reference genome to be used for analysis. The specified genome version **must exist in the database**.

  > ⚠️ **Important:** The selected reference genome version must match the genome version used during read alignment. Mismatches may lead to inaccurate signal interpretation and erroneous results.

- **`-R`**: (**Required parameters**) Defines the region type for analysis. Supported values include `tes`, `tss`,  `genebody` and `exon`. 
- **`-T`**:  (**Required parameters**) Sets the title of the analysis. 
-  **`-I`**: (**Required parameters**) Specifies the bam input file path. The calculation module supports only one BAM file per analysis. Alternatively, you can input two BAM files using the **Signal BAM** and **Input BAM** fields (Signal BAM:Input BAM). In this case, the second BAM file will be used as a background signal for normalization.
-  **`-O`**: (**Required parameters**) Defines the output directory path. 
- **`-DB`**: Specifies the path to a new database that should be integrated into the tool's dependency database. 
-  **`-CP`**: Specifies the path to a user-defined configuration file. 
-  **`-X`**: Specifies the path to a gene list subset for analysis. The default is `all`, meaning all genes will be analyzed. 
-  **`-BS`**: Defines the batch size for gene parallel. 
-  **`-B`**: Specifies the biotype to be analyzed, such as `protein_coding` or non-coding RNAs (e.g., `lncRNA`, `ncRNA`). The default value is `protein_coding`, which is suitable for most standard gene expression and regulatory analyses.
-  **`-F`**: Sets the flanking region size. This parameter defines the number of base pairs to include upstream and downstream of the target genomic feature (e.g., TSS, TES, or gene body). Flanking regions are commonly used in functional element analysis to capture regulatory signals or transcriptional activity adjacent to the feature of interest. By default, the flanking region size is set to **2000 bp**, which typically provides sufficient context for most analyses. Users can adjust this value based on the resolution and scope of their study.
-  **`-N`**: Sets the flanking size factor. This parameter is used specifically in genebody mode to dynamically determine the size of the flanking regions. Instead of using a fixed number of base pairs, the flanking region size is calculated as a proportion of the gene body length: flanking size = gene body length × flanking size factor.
-  **`-DP`**: Controls the resolution of the output coverage matrix. Increasing this value provides more data points and higher plotting precision but significantly increases computation time. 
-  **`-MQ`**: Sets the minimum mapping quality threshold for filtering aligned reads. Reads below this threshold will be excluded. 
-  **`-FL`**: Specifies the fragment length, which should be set according to the library size. This is used for correcting single-end sequencing on the negative strand. 
-  **`-P`**: Defines the number of CPU cores to use for parallel processing. 
-  **`-S`**: Define a scaling factor, for example, for spike-in normalization.
-  **`-NF`**: Whether a new folder will be created to store the results.
- **`-BM`**: Specify the bin scale method. Available options are `max`, `mean`, and `median` (default: `mean`). This determines how values within each bin are summarized.

  **`-CM`**: Whether to perform center alignment for the selected regions during visualization (default: `false`). When enabled, regions will be aligned by their center before plotting.

#### **Computation Module Result Interpretation**

This program processes the input BAM file and generates an intermediate output in the form of a **coverage matrix**. In this matrix:

- **Rows** represent genes.
- **Columns** correspond to data points.
- **Values** indicate the physical coverage over the query regions, which include each region of interest extended by a user-defined upstream and downstream flanking size.

The coverage values within each resolution window are aggregated using the method specified by `-BM` (`mean`, `median`, or `max`; default: `mean`). Unless a spike-in scaling factor is provided, the aggregated coverage values are subsequently normalized to CPM (Counts Per Million) to account for differences in sequencing depth across samples.

If Input BAM is provided, signals will be further corrected based on its values `log2(signal/input)`. 

Alternatively, signals will also be corrected if scale_ratio is specified `signal*scale_ratio`.

Additionally, the program generates a default **JSON configuration file** containing recommended parameters for downstream visualization tasks.

### Read-Aware Shift in ChIP-seq and CUT&Tag Analysis

In ChIP-seq and CUT&Tag analysis, read-aware shift is a critical step in processing sequencing reads to accurately estimate protein binding sites or epigenetic marker positions. Since sequencing reads originate from both strands of DNA fragments, the orientation (forward or reverse strand) causes the 5' end of reads to be offset from the actual binding site. Adjusting read positions enables more precise coverage calculation and peak calling.

Processing Method

1. Strand-Specific Adjustment

- **Forward Strand**: The 5' end of reads is typically upstream of the binding site, so it needs to be shifted downstream (toward the 3' direction) by a certain offset (shift).
- **Reverse Strand**: The 5' end of reads is downstream of the binding site, so it needs to be shifted upstream (toward the 5' direction) by the same offset.

2. Determining the Shift Value

- **Fragment Length**: The shift value is typically based on the estimated DNA fragment length, commonly 100-300 bp for ChIP-seq or 50-150 bp for CUT&Tag. The shift is usually half the fragment length. For example, a 200 bp fragment length implies a shift of approximately 100 bp.

---

### **Visualization Mode based on R**

#### Main Script and Functionality

- `lib/ngsVizPlotMain.R` 

  **`Input`**: Specifies the path to the folder containing input JSON configuration files.

  **`Feature`**: Command-line interface for the visualization module, enabling unified execution of plotting tasks in CLI mode. 

- `lib/processJSONParas.R`

  **`Input`**: Specifies the path to the json configuration file.

  **`Feature`**: Used to read and process the JSON configuration generated by the computation module, defining parameters for downstream visualization tasks.

- `lib/coverageHeatmap.R`: 

  **`Input`**: Specifies the path to the folder containing input JSON configuration files.

  **`Feature`**: Used to generate average coverage plots and heatmaps for visualizing genomic data.

- `lib/qualityControlViz.R`: 

  **`Input`**: Specifies the path to the folder containing input JSON configuration files.

  **`Feature`**: Used to visualize PCA and correlation between coverage profiles across multiple samples for quality control purposes.

- `lib/transform2EnrichedHeatmap.R`

  **`Input`**: Specifies the path to the json configuration file.

  **`Feature`**: This script converts computation results into the input format required by EnrichedHeatmap, and utilizes its internal ComplexHeatmap-based engine for visualization, offering enhanced rendering quality for heatmaps.

#### **Adjustable Plotting Options**

- ##### Output Plot Parameters

  | Parameter          | Description                                                  |
  | ------------------ | ------------------------------------------------------------ |
  | `highwidthdpi`     | Controls the resolution of the output image (higher DPI = better quality). |
  | `file_type`        | Specifies the output image format, such as `tiff`, `pdf`, or `png`. |
  | `top_bottom_ratio` | Adjusts the height ratio between the top average coverage plot and the bottom heatmap. |
  | `SEM`              | Determines whether to display the Standard Error of the Mean (SEM) range. |
  | `smooth`           | Applies smoothing to the plot for a cleaner visual appearance. |

- ##### Gene Split Parameters

  | Parameter     | Description                                                  |
  | ------------- | ------------------------------------------------------------ |
  | `hc_m`        | Specifies the hierarchical clustering method used to group genes based on coverage patterns. |
  | `dist_m`      | Defines the distance metric used for clustering (e.g., Euclidean, Pearson, Spearman). |
  | `cluster_num` | Sets the number of clusters to divide the genes into after hierarchical clustering. |

- ##### Theme Parameters

  | Parameter          | Description                                                  |
  | ------------------ | ------------------------------------------------------------ |
  | `title_size`       | Font size of the plot title.                                 |
  | `legend_size`      | Size of the legend box.                                      |
  | `title_color`      | Color of the plot title text.                                |
  | `dash_color`       | Color of dashed lines used in the plot.                      |
  | `axis_text_x_size` | Font size of x-axis tick labels.                             |
  | `text_family`      | Font family used for all text elements.                      |
  | `axis_text_y_size` | Font size of y-axis tick labels.                             |
  | `dash_size`        | Line width of dashed lines.                                  |
  | `sample_color`     | Color assigned to each sample in the plot.                   |
  | `text_face`        | Font style (e.g., plain, bold, italic) for text elements.    |
  | `split_color`      | Color used to distinguish different clusters or groups.      |
  | `title_hjust`      | Horizontal justification of the plot title (e.g., 0 = left, 0.5 = center). |
  | `border_size`      | Thickness of borders around plot panels or elements.         |
  | `legend_text_size` | Font size of text inside the legend.                         |
  | `line_size`        | Thickness of lines used in the plot.                         |
  | `ystrip_text_size` | Font size of text in the y-axis strip (e.g., facet labels).  |

#### Visualization Input

For visualization input, you only need to specify the path to the generated JSON files, and the script will parse the configuration parameters to create the plots. If users need to fine-tune the output image conditions, they can modify the corresponding parameter values in the configuration file.

Specifically, for multi-sample mode, users can create a new folder, copy the JSON configuration files from multiple analysis results into this path, and then specify this path. The system will automatically identify and analyze the multiple samples.

---

### Command Line Interface (CLI)

1. Run the calculation module

- Specify five mandatory parameter values and optional parameter values as needed. The specific meanings of the parameters are detailed in the **Parameter Explanation Section** below. Examples are provided as follows:

  ```shell
  java -jar NGSViz-1.3.jar \
  -G hg38 \
  -R TSS \
  -T analysis_title \
  -I bam_path \
  -O output_dir
  ```

2. Run the visualization module

- At the end of the calculation module running, a csv file of the coverage matrix and a json format plot configuration file will be generated. You can fine-tune visualization results by modifying the default plotting parameters provided in the JSON configuration file. For detailed explanations of the plotting parameters, refer to the **Parameter Explanation Section**. Use the following command to specify the path to the folder containing the JSON configuration file for visualization. Optionally, you can place JSON configuration files for multiple sample analysis results in the same directory and specify this path in the script to enable integrated visualization across samples.

  ```shell
  Rscript lib/ngsVizPlotMain.R <JSON_path>
  ```

**Visualization Outputs:**

- A single plot of the average coverage profile.

- An integrated plot combining a coverage heatmap with the average coverage.
- Additional plots for sample coverage quality control (QC), including:
  - A PCA plot.
  - A multi-sample coverage correlation plot.

**Note:** QC visualization is only available when more than two samples are provided.

---

### **Graphical User Interface (GUI)**

1. **Launching ngsViz RShiny Interface**

To begin, start the ngsViz RShiny application. This will open the interactive interface where you can access both the calculation, visualization modules and help document.

```
conda activate ngsViz
# Run rshiny
R -e "shiny::runApp('ngsViz-Shiny.R', host='0.0.0.0', port=3838)"
```

After a successful run, the terminal will display:
 Listening on http://0.0.0.0:3838

- If you are using a local Linux desktop environment, open your browser and visit: http://localhost:3838.

- If you are running on a remote server and want to access it with your local browser, set up port forwarding. Then, open your browser and visit: http://localhost:3838.

  ```
  ssh -L 3838:localhost:3838 youruser@yourserver
  ```


2. **Running the Calculation Module**

Click on the **CalculationMode** button on the left panel to enter the calculation module.

**Required Parameters**

Fill in the following fields:

- **Genome Version**: Select the appropriate genome build.
- **Region Type**: Choose the region type for analysis.
- **Analysis Title**: Provide a title for your analysis.
- **BAM File Path**: Enter the full path to a BAM file.
- **Output Directory**: Specify the directory where results will be saved.

**Optional Parameters**

You may adjust the values of optional parameters to customize the analysis according to your needs.

Once all parameters are set, click **Start Analysis** to initiate the computation.

> ⚠️ **Note:** The calculation module supports only one BAM file per analysis. Alternatively, you can input two BAM files using the **Signal BAM** and **Input BAM** fields (Signal BAM:Input BAM). In this case, the second BAM file will be used as a background signal for normalization.

**3. Running the Visualization Module**

After the calculation is complete, proceed to the visualization module.

Click on **VisualizationMode** and choose one of the following visualization options:

**SingleCoveragePlot**

This option visualizes the result of a single sample.

Steps:

1. Enter the absolute path to the `.json` file generated in the result folder.
2. Click **Parsing Parameters**.
3. Adjust the plotting parameters as needed for fine-tuning.
4. Click **Run Plot** to generate and display the coverage plot.
5. If needed, modify parameters and click **Run Plot** again to regenerate the plot.

**MultiSamplePlot**

This option allows visualization of multiple samples.

Steps:

1. Specify one folder containing one or more`.json` configuration files.
2. Click **Parsing Json Config Files**.
3. Generate average coverage plots and integrated visualizations (heatmaps and average coverage plots).

**QCPlot**

This option is designed for quality control across multiple samples.

Steps:

1. Place multiple `.json` configuration files in a single folder.
2. Enter the folder path.
3. Click **Start Parsing to Plot** to generate PCA and correlation plots for sample coverage.

---

## 🧪 Example

### Point Mode

```shell
java -jar NGSViz-1.3.jar \
-G hg38 \
-R tss \
-T H3K4me3 \
-I Samples/K562_H3K4me3-ENCFF752MYF_5p.bam \
-O Samples/Result/H3K4me3_point \
-P 8
Rscript lib/ngsVizPlotMain.R Samples/Result/H3K4me3_point
```

### Broad Mode

```
java -jar NGSViz-1.3.jar \
-G hg38 \
-R genebody \
-T H3K36me3 \
-I Samples/K562_H3K36me3-ENCFF975JFV_5P.bam \
-O Samples/Result/K36me3_broad \
-P 8
Rscript lib/ngsVizPlotMain.R Samples/Result/K36me3_broad
```

### Center Mode

```
java -jar NGSViz-1.3.jar \
-G hg38 \
-R genebody \
-T H3K36me3 \
-I Samples/K562_H3K36me3-ENCFF975JFV_5P.bam \
-O Samples/Result/K36me3_center \
-CM true \
-P 8
Rscript lib/ngsVizPlotMain.R Samples/Result/K36me3_center
```

### Intergenic Mode

```
java -jar NGSViz-1.3.jar \
-G hg38 \
-R genebody \
-T H3K36me3 \
-I Samples/K562_H3K36me3-ENCFF975JFV_5P.bam \
-O Samples/Result/K36me3_intergenic \
-BD database/hg38_intergenic_regions_greater_1kb.bed \
-P 8
Rscript lib/ngsVizPlotMain.R Samples/Result/K36me3_intergenic
```

---

## ❓ Frequently Asked Questions (FAQ)

**Q1:** I get an error saying “Database not found” when running the program.
**A:** Please make sure you have built the reference database using the `buildRefDB.R` script. Then, specify the correct path to the database using the appropriate parameter when running the program.

---

For questions or suggestions, feel free to submit an issue or contact the author, **[Benchen Ye](yc47650@um.edu.mo)**.
