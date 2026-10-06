suppressPackageStartupMessages({library(data.table); library(ggplot2)})
root <- "/Users/benche/Desktop/myProj/ngsPlot/Test/sgNSD1_GSE186239/analysis"
ngsplot_dir <- file.path(root, "ngsplot_ABC_20261004/compute")
ngsviz_file <- file.path(root, "ngsviz_hg38_ABCD_20261004/final/GSE186239_H3K36me2_ABCD_ngsViz_aggregate_profiles.csv")
out_dir <- file.path(root, "ngsplot_ngsviz_profile_correlation_latest_20261004")
dir.create(out_dir, recursive=TRUE, showWarnings=FALSE)
ngsviz <- fread(ngsviz_file)[panel %in% c("A_up", "B_down", "C_genebody")]
map <- data.table(panel=c("A_up", "B_down", "C_genebody"), code=c("A", "B", "C"),
                  gene_set=c("Up-regulated", "Down-regulated", "Gene body"))
pairs <- rbindlist(lapply(seq_len(nrow(map)), function(i) {
  ngsplot <- fread(file.path(ngsplot_dir, map$code[i], "avgprof.txt"))
  rbindlist(lapply(c("NSD1WT", "NSD1KO"), function(g) {
    x <- ngsviz[panel == map$panel[i] & group == g]
    data.table(gene_set=map$gene_set[i], group=g, position=x$x,
               ngsplot=ngsplot[[g]], ngsviz=x$mean, n_genes_ngsviz=unique(x$n))
  }))
}))
pairs[, gene_set := factor(gene_set, c("Up-regulated", "Down-regulated", "Gene body"))]
pairs[, group := factor(group, c("NSD1WT", "NSD1KO"))]
pairs[, segment := cut(position, c(-1,19,80,100), c("Upstream", "Gene body", "Downstream"))]
summary <- pairs[, .(n_points=.N, n_genes_ngsviz=first(n_genes_ngsviz),
  pearson_r=cor(ngsplot, ngsviz), pearson_p=cor.test(ngsplot, ngsviz)$p.value), by=.(gene_set, group)]
summary[, label := sprintf("Pearson r = %.3f", pearson_r)]
p <- ggplot(pairs, aes(ngsplot, ngsviz, color=segment)) + geom_point(size=1.4, alpha=0.8) +
  geom_smooth(method="lm", se=FALSE, color="#333333", linewidth=0.6) +
  geom_text(data=summary, aes(x=-Inf, y=Inf, label=label), inherit.aes=FALSE, hjust=-0.08, vjust=1.15, size=3.1) +
  facet_grid(group ~ gene_set, scales="free") +
  scale_color_manual(values=c("#0072B2", "#009E73", "#D55E00")) +
  labs(x="ngs.plot mean coverage (RPM)", y="ngsViz mean coverage (CPM)", color="Region") +
  theme_bw(base_size=9) + theme(panel.grid=element_blank(), strip.text=element_text(face="bold"), legend.position="bottom")
fwrite(pairs, file.path(out_dir, "profile_point_pairs.csv"))
fwrite(summary[, .(gene_set, group, n_points, n_genes_ngsviz, pearson_r, pearson_p)], file.path(out_dir, "correlation_summary.csv"))
ggsave(file.path(out_dir, "ngsplot_vs_latest_ngsviz_ABC_correlation.pdf"), p, width=180, height=125, units="mm")
ggsave(file.path(out_dir, "ngsplot_vs_latest_ngsviz_ABC_correlation.png"), p, width=180, height=125, units="mm", dpi=300)
