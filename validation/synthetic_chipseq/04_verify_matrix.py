import csv
import sys


def read_expected(path):
    with open(path) as handle:
        rows = list(csv.reader(handle, delimiter="\t"))
    assert len(rows) == 500
    assert all(row[:2] == ["chrSynthetic", str(i + 1)] for i, row in enumerate(rows))
    return [int(row[2]) for row in rows]


def bin_mean(values, points):
    # 使用 ngsViz 整数边界计算每个 bin 的理论均值。
    size = len(values)
    return [sum(values[i * size // points:(i + 1) * size // points]) /
            ((i + 1) * size // points - i * size // points) for i in range(points)]


expected = read_expected(sys.argv[1])
with open(sys.argv[2]) as handle:
    rows = list(csv.reader(handle))
assert len(rows) == 2 and rows[1][0] == "synthetic_full:NM_SYNTHETIC"
observed = [float(value) for value in rows[1][1:]]
assert len(observed) == 501

# 测试区域位于染色体边界；ngsViz 先各裁去 20 bp buffer，再把中段缩放为 301 点。
middle = bin_mean(expected[20:-20], 301)
transformed = [0.0] * 501
transformed[100] = middle[0] / 2
transformed[400] = middle[-1] / 2
transformed[101:400] = middle[1:-1]
errors = [abs(a - b) for a, b in zip(observed, transformed)]
print(f"points=501 mismatches={sum(error > 1e-9 for error in errors)} "
      f"max_error={max(errors):.6f} MAE={sum(errors) / len(errors):.6f}")
assert max(errors) <= 1e-9
