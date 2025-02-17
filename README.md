# NGSVir

## Introduction

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



## Parameter 



| Parameter Name |    Variable Name     |  Default Value   |          Describtion          |
| :------------: | :------------------: | :--------------: | :---------------------------: |
|       -G       |        genome        |        -         | the reference genome database |
|       -R       |     region_type      |        -         | the region type for analysis  |
|       -I       |      input_file      |        -         |        Input file path        |
|       -O       |     output_path      |        -         |          output path          |
|       -D       |       DB_tpye        |      RefSeq      |  Reference sequence database  |
|       -X       |      **gene**s       |     **all**      |    gene list for analysis     |
|       -T       |      **title**       | Average Coverage |        analysis title         |
|       -A       |    analysis_type     |    transcript    |         analysis type         |
|       -B       |       biotype        |  protein_coding  |           gene type           |
|       -F       |     flank_region     |                  |                               |
|       -N       |     flank_factor     |       0.0        |                               |
|      -RB       |        robust        |       0.0        |                               |
|       -S       | random_sampling_rate |       0.0        |                               |
|       -M       |    scaler_method     |       bin        |                               |
|      -CZ       |      chunk_size      |       100        |                               |
|      -MQ       |       min_mapq       |        20        |                               |
|      -FL       |       frag_len       |       150        |                               |
|      -SS       |     strand_spec      |       both       |                               |
|      -FI       |        fi_tag        |        0         |                               |

