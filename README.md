# NGSVir

## Introduction

## Quick Start



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



| Parameter Name |    Variable Name     |  Default Value   |           Describtion            |
| :------------: | :------------------: | :--------------: | :------------------------------: |
|       -G       |        genome        |        -         |  the reference genome database   |
|       -R       |     region_type      |        -         |   the region type for analysis   |
|       -I       |      input_file      |        -         |         Input file path          |
|       -O       |     output_path      |        -         |           output path            |
|       -D       |       DB_tpye        |      RefSeq      |   Reference sequence database    |
|       -X       |      **gene**s       |     **all**      |      gene list for analysis      |
|       -T       |      **title**       | Average Coverage |          analysis title          |
|       -A       |    analysis_type     |    transcript    |          analysis type           |
|       -B       |       biotype        |  protein_coding  |            gene type             |
|       -F       |     flank_region     |       2000       |       Flanking region size       |
|       -N       |     flank_factor     |       0.0        |       Flanking size factor       |
|      -RB       |        robust        |       0.0        | Percentage of both ends trimmed  |
|       -S       | random_sampling_rate |       0.0        |       random sampling rate       |
|       -M       |    scaler_method     |       bin        | method to scale the query region |
|      -CZ       |      chunk_size      |       100        |         Gene chunk size          |
|      -MQ       |       min_mapq       |        20        |     threshold to filter read     |
|      -FL       |       frag_len       |       150        |         Fragment length          |
|      -SS       |     strand_spec      |       both       |     read strand in bam file      |
|       -P       |       core_num       |                  |                                  |

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

