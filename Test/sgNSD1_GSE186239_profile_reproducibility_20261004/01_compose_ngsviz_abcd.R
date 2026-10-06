suppressPackageStartupMessages({library(data.table); library(ggplot2); library(jsonlite); library(patchwork); library(zoo)})
root <- "/Users/benche/Desktop/myProj/ngsPlot/Test/sgNSD1_GSE186239/analysis/ngsviz_hg38_ABCD_20261004"
panels <- c(A_up="H3K36me2 - Up Regulated Genes", B_down="H3K36me2 - Down Regulated Genes",
            C_genebody="H3K36me2 - Genebody Region", D_intergenic="H3K36me2 - Intergenic Region")
profile <- function(folder) {
  files <- sort(list.files(file.path(root, "visualization", folder), "_NGSViz_plotSetting.json$", full.names=TRUE))
  rbindlist(lapply(files, function(file) {
    cfg <- fromJSON(file)
    mat <- as.matrix(fread(cfg$OutputFile$heatmapDataFile, check.names=FALSE)[, -1])
    mat <- t(apply(mat, 1, function(x) rollmean(c(rep(x[1], 2), x, rep(x[length(x)], 2)), 5)))
    data.table(x=0:100, mean=colMeans(mat), sem=apply(mat, 2, sd)/sqrt(nrow(mat)),
               group=cfg$run_metadata$group_name, panel=folder, n=nrow(mat))
  }))
}
all <- rbindlist(lapply(names(panels), profile))
out <- file.path(root, "final")
dir.create(out, recursive=TRUE, showWarnings=FALSE)
fwrite(all, file.path(out, "GSE186239_H3K36me2_ABCD_ngsViz_aggregate_profiles.csv"))
make_plot <- function(id) {
  d <- all[panel == id]
  labels <- if (id == "D_intergenic") c("-2000", "End", "Start", "2000") else c("-2000", "TSS", "TES", "2000")
  ggplot(d, aes(x, mean, color=group)) + geom_line(linewidth=0.8) +
    scale_color_manual(values=c(NSD1KO="#BC3C29", NSD1WT="#0072B5"), breaks=c("NSD1KO", "NSD1WT")) +
    scale_x_continuous(breaks=c(0,20,80,100), labels=labels, expand=expansion(mult=c(0.03,0.03))) +
    geom_vline(xintercept=c(20,80), linetype="dashed", linewidth=0.55) +
    labs(title=panels[[id]], x=NULL, y=NULL, color="Group") +
    theme_classic(base_size=11) + theme(plot.title=element_text(face="bold", hjust=0.5, size=11),
      axis.text=element_text(face="bold", color="#4D4D4D"), axis.line=element_line(linewidth=0.8),
      axis.ticks=element_line(linewidth=0.8), legend.title=element_text(face="bold"), legend.text=element_text(face="bold"))
}
plots <- lapply(names(panels), make_plot)
figure <- wrap_plots(plots, ncol=2, guides="collect") + plot_annotation(tag_levels="A") & theme(legend.position="right")
ggsave(file.path(out, "GSE186239_H3K36me2_ABCD_ngsViz.pdf"), figure, width=9, height=7.2, units="in")
ggsave(file.path(out, "GSE186239_H3K36me2_ABCD_ngsViz.png"), figure, width=9, height=7.2, units="in", dpi=300)
