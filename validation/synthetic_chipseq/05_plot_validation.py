import csv
import sys
import matplotlib.pyplot as plt
import numpy as np

sam, expected_file, ngsviz_file, bedtools_file, output = sys.argv[1:]
reads = []
with open(sam) as handle:
    for row in csv.reader((line for line in handle if not line.startswith("@")), delimiter="\t"):
        reads.append((row[0], int(row[1]), int(row[3]), int(row[3]) + 19))

def coverage(path, header=False):
    with open(path) as handle:
        rows = list(csv.reader(handle, delimiter="\t"))
    if header:
        rows = rows[1:]
    return np.array([int(row[2]) for row in rows])

expected = coverage(expected_file)
ngsviz = coverage(ngsviz_file, True)
bedtools = coverage(bedtools_file)
x = np.arange(1, 501)
blue, orange, green, grey = "#0072B2", "#D55E00", "#009E73", "#4D4D4D"
plt.rcParams.update({"font.family": "sans-serif", "font.size": 8, "axes.labelsize": 9,
                     "axes.spines.top": False, "axes.spines.right": False})
fig, axes = plt.subplots(2, 2, figsize=(7.1, 5.8), constrained_layout=True)

ax = axes[0, 0]
for left, right, label in [(90, 160, "Cluster A"), (190, 235, "Cluster B"),
                           (290, 350, "Cluster C"), (390, 445, "Cluster D")]:
    ax.axvspan(left, right, color="#D9D9D9", alpha=0.18, lw=0)
    ax.text((left + right) / 2, 0.6, label, ha="center", va="bottom", fontsize=7)
for lane, (name, flag, start, end) in enumerate(reads[::-1], 1):
    color = orange if flag & 16 else blue
    ax.plot([start, end], [lane, lane], color=color, lw=2.2)
    ax.plot(end if not flag & 16 else start, lane, marker=">" if not flag & 16 else "<", color=color, ms=4)
ax.set(xlim=(80, 455), ylim=(0, 19), xlabel="Position on chrSynthetic (bp)", ylabel="Synthetic reads")
ax.set_yticks([])
ax.plot([], [], color=blue, lw=2, marker=">", label="Forward")
ax.plot([], [], color=orange, lw=2, marker="<", label="Reverse")
ax.legend(frameon=False, ncol=2, loc="upper right")

ax = axes[0, 1]
ax.step(x, expected, where="mid", color=grey, lw=1.8, label="Analytical expectation")
ax.plot(x[::5], ngsviz[::5], "o", ms=3.2, mfc="none", mec=blue, label="ngsViz")
ax.plot(x[2::5], bedtools[2::5], "x", ms=3.2, color=orange, label="BEDTools")
ax.set(xlim=(1, 500), ylim=(-0.15, 5.5), xlabel="Position on chrSynthetic (bp)", ylabel="Coverage depth")
ax.legend(frameon=False, fontsize=7)

ax = axes[1, 0]
cluster_b = [read for read in reads if read[0].startswith("B")]
for lane, (name, flag, start, end) in enumerate(cluster_b[::-1], 1):
    y = -lane
    color = orange if flag & 16 else blue
    ax.plot([start, end], [y, y], color=color, lw=2.5)
    ax.text(189.5, y, name, ha="right", va="center", fontsize=7)
ax.step(x[189:235], expected[189:235], where="mid", color=green, lw=2)
ax.text(203, 4.2, "4", color=green, ha="center")
ax.text(213, 5.2, "5", color=green, ha="center")
ax.text(223, 1.2, "1", color=green, ha="center")
ax.axhline(0, color="#BDBDBD", lw=0.7)
ax.set(xlim=(190, 235), ylim=(-5.8, 5.8), xlabel="Position on chrSynthetic (bp)", ylabel="Reads / coverage")
ax.set_yticks([0, 1, 4, 5])

ax = axes[1, 1]
err_ngs = np.abs(ngsviz - expected)
err_bed = np.abs(bedtools - expected)
ax.plot(x, err_ngs, color=blue, lw=1.5, label="|ngsViz − expected|")
ax.plot(x, err_bed, color=orange, lw=1, ls="--", label="|BEDTools − expected|")
ax.set(xlim=(1, 500), ylim=(-0.02, 0.25), xlabel="Position on chrSynthetic (bp)", ylabel="Absolute error")
ax.text(250, 0.15, "500/500 positions matched\nMismatches = 0\nMaximum error = 0; MAE = 0",
        ha="center", va="center", fontsize=8)
ax.legend(frameon=False, fontsize=7, loc="upper right")

for label, ax in zip("ABCD", axes.flat):
    ax.text(0, 1.04, label, transform=ax.transAxes, fontsize=11, fontweight="bold", va="bottom")
fig.savefig(f"{output}.pdf", dpi=300, bbox_inches="tight")
fig.savefig(f"{output}.png", dpi=300, bbox_inches="tight")
fig.savefig(f"{output}.tiff", dpi=600, bbox_inches="tight", pil_kwargs={"compression": "tiff_lzw"})
