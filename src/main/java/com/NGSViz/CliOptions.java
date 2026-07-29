package com.NGSViz;

import java.util.Arrays;

/**
 * Handles command-line options that do not start an analysis.
 */
public final class CliOptions {
    public static final String VERSION = "1.3";

    private CliOptions() {
    }

    public static boolean handleInformationalOption(String[] args) {
        if (Arrays.asList(args).contains("-H")) {
            printHelp();
            return true;
        }
        if (Arrays.asList(args).contains("-V")) {
            System.out.println("ngsViz " + VERSION);
            return true;
        }
        return false;
    }

    private static void printHelp() {
        System.out.println(
                "Usage:\n"
                + "  java -jar NGSViz-1.3.jar -G <genome> -R <region> -I <input> "
                + "-O <output> -T <title> [options]\n\n"
                + "Information:\n"
                + "  -H              Print all command usage help\n"
                + "  -V              Print the current version\n"
                + "  -J <yaml>       Path to the YAML configuration supplied by AI\n\n"
                + "Required analysis parameters:\n"
                + "  -G <genome>     Reference genome version\n"
                + "  -R <region>     Region type\n"
                + "  -I <input>      BAM, TXT, or CSV input path\n"
                + "  -O <output>     Output directory\n"
                + "  -T <title>      Analysis title\n\n"
                + "Optional parameters:\n"
                + "  -DB <path>      Database to merge\n"
                + "  -CP <path>      System JSON configuration path\n"
                + "  -D <type>       Reference database type\n"
                + "  -X <genes>      Gene list or gene-list path (default: all)\n"
                + "  -A <type>       Analysis type: transcript or exon (default: transcript)\n"
                + "  -B <biotype>    Gene biotype (default: protein_coding)\n"
                + "  -F <bp>         Flanking region size (default: region dependent)\n"
                + "  -N <factor>     Flanking size factor (default: 0.0)\n"
                + "  -S <ratio>      Signal scale ratio\n"
                + "  -BD <path>      Custom BED database path\n"
                + "  -DP <number>    Number of data points (default: 100)\n"
                + "  -P <number>     Number of CPU cores (default: 1)\n"
                + "  -BM <method>    Bin method: mean, median, or max (default: mean)\n"
                + "  -BS <number>    Gene batch size (default: 500)\n"
                + "  -MQ <number>    Minimum mapping quality (default: 20)\n"
                + "  -FL <bp>        Single-end fragment length (default: 150)\n"
                + "  -SS <mode>      Strand mode: both, same, or opposite (default: both)\n"
                + "  -NF <boolean>   Create a title-named result directory (default: false)\n"
                + "  -CM <boolean>   Align regions by center (default: false)"
        );
    }
}
