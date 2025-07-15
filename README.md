# NGSVir User Guide

---

## Table of Contents

- [Introduction](#introduction)
- [Installation](#installation)
- [Quick Start](#quick-start)
- [Main modules and script description](#Main modules and script description)
  - [1. Java Calculation Module (`NGSViz-1.0-SNAPSHOT.jar`)](#1-java-calculation-module-ngsviz-10-snapshotjar)
  - [2. R Shiny Interactive Interface](#2-r-shiny-interactive-interface)
  - [3. Visualization and Analysis R Scripts](#3-visualization-and-analysis-r-scripts)
  - [4. Reference Database Construction Script](#4-reference-database-construction-script)
- [Parameter Descriptions](#parameter-descriptions)
- [Examples](#examples)
- [FAQ](#faq)
- [References & Acknowledgements](#references--acknowledgements)

---

## Introduction

**NGSViz** is an analysis and visualization tool for high-throughput sequencing (NGS) data, supporting input files in BAM format, integrating an efficient Java computing engine, flexible R visualization scripts, and an interactive R Shiny interface, providing a variety of commonly used built-in genomic functional element databases, suitable for various scenarios such as genomic coverage and enrichment analysis.

###  **Typical Analysis Workflow**

1. **Prepare the Reference Database**

   - Build the reference database using the script `lib/buildRefDB.R`, or download a pre-built version from the provided link. In addition, users can build their own reference databases by following the format and rules provided by this program.
   - Ensure the database path is correctly specified in the system configuration file (`NGSViz_setting.json`) or via the `-DB` parameter.

2. **Prepare BAM files to analyse.** 

   > ⚠️ **Note:** BAM files require a corresponding bam index file (bai) to be provided. If a BAM index is not available, it must be generated using SAMtools.

3. **Launch the RShiny Interface or Run via Command Line**

   - Start the program either through the RShiny interface or by executing the command-line tool with appropriate parameters.

4. **Set Required Parameters**

   - Specify the reference genome version (`-G`), region type (`-R`), analysis title (`-T`), input file (`-I`), and output path (`-O`).
   - Optionally, define number of CPU cores to use (`-P`), biotype (`-B`), and other advanced parameters.

5. **Run the Calculation Module**

   - Execute the analysis by clicking **Start Analysis** in the RShiny interface or running the command-line tool.
   - The program will process the input BAM file and generate a coverage matrix and JSON configuration file used for downstream visualization tasks.

6. **Visualize the Results**

   - **SingleCoveragePlot** for individual samples.
   - **MultiSamplePlot** for average and integrated views.
   - **QCPlot** for PCA and correlation analysis across samples.

---

## Installation

### GitHub installation

#### 1. Clone code repository

```bash
git clone git@github.com:shaolab-UM/NGSViz.git
cd NGSViz
```

#### 2. Install dependency environment

- All dependent runtime environments are recommended to be installed using conda by `conda env create -f requirement.yml`
  - Install the Java runtime environment; it is recommended Java 8 and above versions
  - Install R and its dependencies, R version 4.0 and above


#### 4. Other Dependencies

- It is recommended to install commonly used bioinformatics tools such as samtools (if needed to process BAM files).

#### 5. Set software global configuration

- To modify the global configuration file `NGSViz_setting.json`, update the values of `tool_path`, `db_path`, and `java_path`, which correspond to the tool's installation path, the path to the required database, and the path to the Java environment, respectively.

  If you do not have permission to edit the original configuration file, you can create a new JSON configuration file with the following content. When running the computation module of ngsViz, use the `-CP` parameter to specify the path to your newly created configuration file.

  ```json
  {
      "versionNum": {
          "version": "1.0"
      },
      "toolParas": {
          "tool_path": "/yourPath/NGSViz",
          "db_path": "/yourPath/NGSViz/database/genomeCoordinate.db",
          "tool_name": "NGSViz-1.0.jar",
          "java_path": "/usr/bin/java"
      }
  }
  ```

---

### **Install and run using a container**

#### Run with docker

```shell
# pull the docker image of ngsViz
docker pull imageName/imageId
# build the docker container
docker run 
--appendonly yes
--name containerName 
--ipc host 
-dit imageName/imageId
# Start container
docker start containerName 
# Enter the container
docker attach containerName 
```



#### Run with singularity



#### Run with Apptainer

If the system lacks a container runtime environment, it is recommended to use Conda to install `Apptainer`, a container platform designed for secure and portable high-performance computing. Apptainer is designed to enable unprivileged users (without root privileges) to run containers securely and efficiently. Below, we provide some simple examples. For more advanced usage, please refer to the official documentation. 

```shell
# install the Apptainer
conda install conda-forge::apptainer
# 1.1 Pull Docker container image
apptainer pull docker://ubuntu:20.04
# 1.2 Run Docker container
apptainer run docker://ubuntu:20.04
# 2. Run Singularity container
apptainer run ubuntu_20.04.sif
```



---

## Main modules and script description

### Database

#### 🧬 Chromosome Naming Compatibility When Processing BAM Files

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
| 2      | CoordinateTblName | Table name containing genomic feature coordinates            |
| 3      | DB                | Source of annotation database (e.g., RefSeq)                 |
| 4      | Genome            | Reference genome version (e.g., hg38, mm10)                  |
| 5      | Region            | Type of genomic region (e.g., gene body, tss, tes, exon)     |
| 6      | AnalysisType      | Type of analysis performed (e.g., exon or transcript)        |
| 7      | Biotype           | Gene classification (e.g., protein-coding, lncRNA, pseudogene) |
| 8      | PointLab          | Labeled point of interest (e.g., TSS, TES)                   |
| 9      | FlankSize         | Flanking region size added upstream and downstream of the query region |
| 10     | DefaultChoose     | Indicates the default transcript selected when multiple exist (longest one) |

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

**Note:** All pre-built databases can be used independently. To activate a downloaded database, simply specify its path in the system configuration settings `NGSViz_setting.json`. Once configured, the program will recognize and utilize the selected database without requiring additional setup.

Pre-built database of genomic functional elements

|  Species  |         Species         | Genome Version |
| :-------: | :---------------------: | :------------: |
|   Human   |      Homo_sapiens       |      hg38      |
|   Human   |      Homo_sapiens       |      t2t       |
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
|    Dog    |    Canis_familiaris     |    canFam6     |

#### Modify the build-in batabase path

The default built-in database, `genomeCoordinate.db`, is located in the program’s `database`directory. Its location is defined in the system configuration file `NGSViz_setting.json`, which resides in the system settings path relative to the program directory.

However, in environments such as containers, this database may be mounted in read-only mode, preventing users from adding new databases using the `merge` method. To address this, the program allows users to create a custom `NGSViz_setting.json` file at any location. When launching the program, users can specify the path to this custom configuration file using the `-CP` parameter or through the RShiny interface. Within this file, the `db_path` parameter can be modified to point to the desired reference database location.

#### Merge database

To merge a new database into the program’s existing reference database, use the `-DB` parameter to specify the path of the database to be imported. The database can either be downloaded from our cloud storage or built manually using the provided script `lib/buildRefDB.R`.

The target database for merging is determined by the `db_path` specified in the ngsViz system configuration file.

By specifying the database path to be imported using the `-DB` parameter, a database can be merged into the database that the program depends on. The database to be imported can be downloaded from our cloud storage, or customize the build according to our build database script`lib/buildRefDB.R`. The database of this program is specified in the path of the ngsViz system configuration.

```shell
inport_db_path=$YOUR_PATH/NGSViz_hg19_RefSeq.db
java -jar ngsViz_1.0.jar -DB $inport_db_path
```

#### Build a reference database (optional)

If the pre-built databases do not include the species or genome version you require, you can build a custom reference database using the script `lib/buildRefDB.R`. This script constructs the database based on a genomic annotation file in GTF format.

Usage:

```bash
Rscript lib/buildRefDB.R -g reference.gtf -o database/
```

**Parameter Description**

| Parameter |            Description            |       Example       |
| :-------: | :-------------------------------: | :-----------------: |
|    -g     | Reference genome annotation (GTF) | -g Homo_sapiens.gtf |
|    -o     | Output directory for the database |    -o database/     |

#### **Custom Functional Element Database**

In addition to the built-in databases, the program also supports user-defined functional element databases. Users can construct their own databases following the required format and specify the path using the `-BD` parameter when running the program.

- The custom database must be **tab-delimited** and **must not contain a header row**.
- Each row should represent a functional genomic element.
- The database must not contain redundant entries, as the program will read and process all records sequentially.

| Column |          Description          |
| :----: | :---------------------------: |
|   1    |          Chromosome           |
|   2    |        Start Position         |
|   3    |         End Position          |
|   4    | Strand Direction (`+` or `-`) |
|   5    |           Gene Name           |
|   6    |         Transcript ID         |



---

### Computational Module Based on Java

#### Feature

- Efficiently process BAM files, calculate genomic coverage.
- Multithreading concurrency, supports large-scale data
- No environment dependencies

#### Usage

```bash
java -jar NGSViz-1.0-SNAPSHOT.jar [参数]
```

#### Parameters

| Parameter Name |  Variable Name  | Default Value  |                Describtion                 |
| :------------: | :-------------: | :------------: | :----------------------------------------: |
|       -G       |     genome      |       -        |         Reference genome database          |
|       -R       |   region_type   |       -        |          Region type for analysis          |
|       -T       |      title      |       -        |               Analysis title               |
|       -I       |   input_file    |       -        |             Path to input file             |
|       -O       |   output_path   |       -        |          Path to output directory          |
|      -DB       |  input_db_path  |       -        |        Path to database for merging        |
|      -CP       | sys_config_path |       -        |     Path to system configuration file      |
|      -BD       |   bedDB_path    |       -        | Path to custom functional element database |
|       -X       |      genes      |      all       |         List of genes for analysis         |
|       -A       |  analysis_type  |   transcript   |              Type of analysis              |
|       -B       |     biotype     | protein_coding |                Gene biotype                |
|       -F       |  flank_region   |      2000      |        Flanking region size (in bp)        |
|       -N       |  flank_factor   |      0.0       |        Flanking size scaling factor        |
|      -DP       | num_datapoints  |      100       |       Number of data points to plot        |
|      -MQ       |    min_mapq     |       20       |     Minimum mapping quality threshold      |
|      -FL       |    frag_len     |      150       |              Fragment length               |
|       -P       |    core_num     |       1        |         Number of CPU cores to use         |
|       -S       |   scale_ratio   |       1        |     Scaling ratio for signal intensity     |

#### Command-Line Parameters Description

- **`-G`**: (**Required parameters**) Indicates the version of the reference genome to be used for analysis. The specified genome version **must exist in the database**.

  > ⚠️ **Important:** The selected reference genome version must match the genome version used during read alignment. Mismatches may lead to inaccurate signal interpretation and erroneous results.

- **`-R`**: (**Required parameters**) Defines the region type for analysis. Supported values include `tes`, `tss`, and `genebody`. 
- **`-T`**:  (**Required parameters**) Sets the title of the analysis. 
-  **`-I`**: (**Required parameters**) Specifies the bam input file path. The calculation module supports only one BAM file per analysis. Alternatively, you can input two BAM files using the **Signal BAM** and **Input BAM** fields (Signal BAM:Input BAM). In this case, the second BAM file will be used as a background signal for normalization.
-  **`-O`**: (**Required parameters**) Defines the output directory path. 
- **`-DB`**: Specifies the path to a new database that should be integrated into the tool's dependency database. 
-  **`-CP`**: Specifies the path to a user-defined configuration file. 
-  **`-X`**: Specifies the path to a gene list subset for analysis. The default is `all`, meaning all genes will be analyzed. 
-  **`-A`**: Defines the analysis type, either `transcript` or `exon`. 
-  **`-B`**: Specifies the biotype to be analyzed, such as `protein_coding` or non-coding RNAs (e.g., `lncRNA`, `ncRNA`). The default value is `protein_coding`, which is suitable for most standard gene expression and regulatory analyses.
-  **`-F`**: Sets the flanking region size. This parameter defines the number of base pairs to include upstream and downstream of the target genomic feature (e.g., TSS, TES, or gene body). Flanking regions are commonly used in functional element analysis to capture regulatory signals or transcriptional activity adjacent to the feature of interest. By default, the flanking region size is set to **2000 bp**, which typically provides sufficient context for most analyses. Users can adjust this value based on the resolution and scope of their study.
-  **`-N`**: Sets the flanking size factor. This parameter is used specifically in genebody mode to dynamically determine the size of the flanking regions. Instead of using a fixed number of base pairs, the flanking region size is calculated as a proportion of the gene body length: flanking size = gene body length × flanking size factor.
-  **`-DP`**: Controls the resolution of the output coverage matrix. Increasing this value provides more data points and higher plotting precision but significantly increases computation time. 
-  **`-MQ`**: Sets the minimum mapping quality threshold for filtering aligned reads. Reads below this threshold will be excluded. 
-  **`-FL`**: Specifies the fragment length, which should be set according to the library size. This is used for correcting single-end sequencing on the negative strand. 
-  **`-P`**: Defines the number of CPU cores to use for parallel processing. 
-  **`-S`**:Define a scaling factor, for example, for spike-in normalization.

#### **Computation Module Result Interpretation**

This program processes the input BAM file and generates an intermediate output in the form of a **coverage matrix**. In this matrix:

- **Rows** represent genes.
- **Columns** correspond to data points.
- **Values** indicate the physical coverage over the query regions, which include each region of interest extended by a user-defined upstream and downstream flanking size.

The coverage values are scaled according to the resolution of each data point

The coverage values are scaled using the **average coverage** within each data point's resolution window  and normalized using **CPM (Counts Per Million)** to ensure comparability across samples. This ensures that the resulting matrix reflects normalized physical coverage across query regions, accounting for differences in sequencing depth and resolution.

Additionally, the program generates a default **JSON configuration file** containing recommended parameters for downstream visualization tasks.



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

### Command Line Interface (CLI)

1. Run the calculation module

- Specify five mandatory parameter values and optional parameter values as needed. The specific meanings of the parameters are detailed in the **Parameter Explanation Section** below. Examples are provided as follows:

  ```shell
  java -jar NGSViz-1.0-SNAPSHOT.jar 
  -G hg38 
  -R TSS 
  -T analysis_title
  -I bam_path
  -O output_dir
  ```

2. Run the visualization module

- At the end of the calculation module running, a csv file of the coverage matrix and a json format plot configuration file will be generated. You can fine-tune visualization results by modifying the default plotting parameters provided in the JSON configuration file. For detailed explanations of the plotting parameters, refer to the **Parameter Explanation Section**. Use the following command to specify the path to the folder containing the JSON configuration file for visualization. Optionally, you can place JSON configuration files for multiple sample analysis results in the same directory and specify this path in the script to enable integrated visualization across samples.

  ```shell
  Rscript lib/ngsVizPlotMain.R $JSON_path
  ```

**Visualization Outputs:**

- A single plot of the average coverage profile.

- An integrated plot combining a coverage heatmap with the average coverage.
- Additional plots for sample coverage quality control (QC), including:
  - A PCA plot.
  - A multi-sample coverage correlation plot.

**Note:** QC visualization is only available when more than two samples are provided.



### **Graphical User Interface (GUI)**

1. **Launching ngsViz RShiny Interface**

To begin, start the ngsViz RShiny application. This will open the interactive interface where you can access both the calculation, visualization modules and help document.

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

#### 







## **Example**

### 1. Point Mode - H3K4me3



```shell
java -jar NGSViz-1.0.jar 
-G hg19 
-R TSS 
-T H3K4me3
-I bam_path
-O output_dir
-P 8
```





```bash
# 1. 构建数据库
Rscript lib/buildRefDB.R -g Homo_sapiens.gtf -o database/

# 2. 运行主计算
java -jar NGSViz-1.0-SNAPSHOT.jar -i sample1.bam -o results/ -c NGSViz_setting.json

# 3. 生成热图
Rscript lib/plot/coverageHeatmap.R -i results/coverage_matrix.csv -o results/heatmap.pdf

# 4. 启动R Shiny界面
cd Viz
Rscript app_副本.R
```

### 



## **Frequently Asked Questions (FAQ)**

**Q1:** I get an error saying “Database not found” when running the program.
**A:** Please make sure you have built the reference database using the `buildRefDB.R` script. Then, specify the correct path to the database using the appropriate parameter when running the program.

---



## **References and Acknowledgments**

- This project draws inspiration from several excellent open-source tools, including deepTools and ngs.plot.
- For questions or suggestions, feel free to submit an issue or contact the author, **[Benchen Ye](yc47650@um.edu.mo)**.



