#!/usr/bin/env python3
"""Read TRTR item/block PNGs and emit a pixel-level visual report.

Does not rely on a human looking at the files: every claim is derived from
RGBA samples. Also writes a 16x scaled nearest-neighbor preview PNG per
texture so the palette/silhouette can be inspected without opening the
original 16x16 source.
"""

from __future__ import annotations

import colorsys
from collections import Counter
from pathlib import Path

from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
TEX_ROOT = ROOT / "src" / "main" / "resources" / "assets" / "trtr" / "textures"
OUT_DIR = ROOT / "scripts" / "_texture_inspect"
SCALE = 16

# Named anchors used only to label clusters, not to invent pixels.
NAMED = {
    "black": (20, 20, 20),
    "charcoal": (40, 40, 40),
    "dark-gray": (70, 70, 70),
    "gray": (110, 110, 110),
    "light-gray": (160, 160, 160),
    "white": (230, 230, 230),
    "stone": (128, 128, 128),
    "flint-dark": (45, 45, 52),
    "flint": (70, 72, 78),
    "flint-light": (120, 124, 130),
    "wood-dark": (70, 42, 18),
    "wood": (120, 75, 32),
    "wood-light": (170, 115, 55),
    "fiber-dark": (50, 90, 30),
    "fiber": (90, 140, 45),
    "fiber-light": (140, 180, 70),
    "olive": (110, 120, 50),
    "brown": (100, 60, 30),
    "tan": (180, 140, 90),
    "yellow": (200, 180, 50),
    "green": (60, 140, 50),
    "blue-gray": (80, 90, 110),
    "purple-gray": (90, 80, 100),
}


def rgba_list(img: Image.Image) -> list[tuple[int, int, int, int]]:
    raw = img.convert("RGBA").tobytes()
    return [tuple(raw[i : i + 4]) for i in range(0, len(raw), 4)]


def dist2(a: tuple[int, int, int], b: tuple[int, int, int]) -> int:
    return sum((x - y) ** 2 for x, y in zip(a, b))


def name_color(rgb: tuple[int, int, int]) -> str:
    return min(NAMED.items(), key=lambda kv: dist2(rgb, kv[1]))[0]


def hsl(rgb: tuple[int, int, int]) -> tuple[int, int, int]:
    r, g, b = (c / 255.0 for c in rgb)
    h, l, s = colorsys.rgb_to_hls(r, g, b)
    return round(h * 360), round(s * 100), round(l * 100)


LUMA_GLYPHS = " ░▒▓█"


def luma(rgb: tuple[int, int, int]) -> float:
    r, g, b = rgb
    return 0.2126 * r + 0.7152 * g + 0.0722 * b


def glyph_for(rgb: tuple[int, int, int], a: int) -> str:
    if a < 16:
        return "·"
    h, s, l = hsl(rgb)
    if s < 12:
        # 8 luma buckets so a full-canvas gray field still shows structure.
        idx = min(4, int(luma(rgb) / 51))
        return LUMA_GLYPHS[idx]
    if h < 30 or h >= 330:
        return "■"  # red / brown-red
    if h < 70:
        return "▤"  # orange / wood
    if h < 160:
        return "▥"  # yellow-green / fiber
    if h < 260:
        return "▦"  # blue
    return "▧"  # purple


def luma_glyph(rgb: tuple[int, int, int], a: int) -> str:
    if a < 16:
        return "·"
    idx = min(4, int(luma(rgb) / 51))
    return LUMA_GLYPHS[idx]


def mse_rgb(a: list[tuple[int, int, int, int]], b: list[tuple[int, int, int, int]]) -> float:
    n = min(len(a), len(b))
    if n == 0:
        return 0.0
    acc = 0.0
    for i in range(n):
        acc += sum((a[i][c] - b[i][c]) ** 2 for c in range(3))
    return acc / (n * 3)


def local_var(pixels: list[tuple[int, int, int, int]], w: int, h: int) -> float:
    """Mean 3x3 luminance variance — high on noise, low on flat fills, mid on sprites."""
    acc = 0.0
    n = 0
    for y in range(1, h - 1):
        for x in range(1, w - 1):
            vals = [
                luma(pixels[(y + dy) * w + (x + dx)][:3])
                for dy in (-1, 0, 1)
                for dx in (-1, 0, 1)
            ]
            mean = sum(vals) / 9
            acc += sum((v - mean) ** 2 for v in vals) / 9
            n += 1
    return acc / n if n else 0.0


def bbox(pixels: list[tuple[int, int, int, int]], w: int, h: int) -> tuple[int, int, int, int] | None:
    xs, ys = [], []
    for y in range(h):
        for x in range(w):
            if pixels[y * w + x][3] >= 16:
                xs.append(x)
                ys.append(y)
    if not xs:
        return None
    return min(xs), min(ys), max(xs), max(ys)


def analyze(path: Path) -> str:
    img = Image.open(path)
    raw_mode = img.mode
    img = img.convert("RGBA")
    w, h = img.size
    pixels = rgba_list(img)
    opaque = [(x, y, p) for y in range(h) for x in range(w) if (p := pixels[y * w + x])[3] >= 16]
    trans = w * h - len(opaque)

    palette = Counter((p[0], p[1], p[2], p[3]) for _, _, p in opaque)
    unique_rgb = Counter((p[0], p[1], p[2]) for _, _, p in opaque)

    # Quantize to 16 colors for a readable palette dump (source stays untouched).
    q = img.convert("P", palette=Image.Palette.ADAPTIVE, colors=16).convert("RGBA")
    qpix = rgba_list(q)
    qcount = Counter((p[0], p[1], p[2]) for i, p in enumerate(qpix) if pixels[i][3] >= 16)

    box = bbox(pixels, w, h)
    fill = 0.0
    if box:
        bw = box[2] - box[0] + 1
        bh = box[3] - box[1] + 1
        fill = len(opaque) / (bw * bh)

    # Row occupancy: which rows actually have ink.
    row_fill = [
        sum(1 for x in range(w) if pixels[y * w + x][3] >= 16) for y in range(h)
    ]
    col_fill = [
        sum(1 for y in range(h) if pixels[y * w + x][3] >= 16) for x in range(w)
    ]

    # Mean color of opaque pixels.
    if opaque:
        mr = sum(p[0] for _, _, p in opaque) / len(opaque)
        mg = sum(p[1] for _, _, p in opaque) / len(opaque)
        mb = sum(p[2] for _, _, p in opaque) / len(opaque)
        mean = (round(mr), round(mg), round(mb))
    else:
        mean = (0, 0, 0)

    # Diagonal bias: NW-SE vs NE-SW occupancy. Tools usually sit on a diagonal.
    nw_se = ne_sw = 0
    for x, y, _ in opaque:
        if abs(x - y) <= 2:
            nw_se += 1
        if abs(x - (h - 1 - y)) <= 2:
            ne_sw += 1
    diag = "none"
    if nw_se > ne_sw * 1.4 and nw_se > len(opaque) * 0.25:
        diag = "NW-SE (\\) like a vanilla tool"
    elif ne_sw > nw_se * 1.4 and ne_sw > len(opaque) * 0.25:
        diag = "NE-SW (/) like a flipped tool"

    lines: list[str] = []
    rel = path.relative_to(TEX_ROOT).as_posix()
    lines.append(f"## {rel}")
    lines.append(f"- file: {path.name}  size={w}x{h}  mode={raw_mode}  bytes={path.stat().st_size}")
    lines.append(f"- opaque={len(opaque)}/{w*h}  transparent={trans}  unique RGBA={len(palette)}  unique RGB={len(unique_rgb)}")
    if box:
        lines.append(
            f"- bbox=({box[0]},{box[1]})-({box[2]},{box[3]})  "
            f"{box[2]-box[0]+1}x{box[3]-box[1]+1}  fill={fill:.2f}"
        )
    else:
        lines.append("- bbox=empty")
    mh, ms, ml = hsl(mean)
    lines.append(f"- mean opaque RGB=#{mean[0]:02X}{mean[1]:02X}{mean[2]:02X}  ~{name_color(mean)}  H={mh} S={ms} L={ml}")
    lines.append(f"- diagonal: {diag}  (NW-SE={nw_se} NE-SW={ne_sw})")
    lines.append(f"- row occupancy: {row_fill}")
    lines.append(f"- col occupancy: {col_fill}")

    lines.append("- top unique RGB (count, hex, named):")
    for rgb, n in unique_rgb.most_common(12):
        hx = f"#{rgb[0]:02X}{rgb[1]:02X}{rgb[2]:02X}"
        lines.append(f"    {n:4d}  {hx}  {name_color(rgb)}")

    lines.append("- adaptive 16-color clusters (opaque only):")
    for rgb, n in qcount.most_common(16):
        hx = f"#{rgb[0]:02X}{rgb[1]:02X}{rgb[2]:02X}"
        lines.append(f"    {n:4d}  {hx}  {name_color(rgb)}")

    lv = local_var(pixels, w, h)
    lines.append(f"- 3x3 luma variance={lv:.1f}  (noise typically >40, painted sprite ~10-30, flat ~0)")
    if trans == 0:
        lines.append("- NOTE: fully opaque canvas — no item silhouette / no vanilla-style alpha")

    lines.append("- hue map (·=air  ░▒▓█=gray luma  ■=red/brown  ▤=orange/wood  ▥=yellow-green  ▦=blue  ▧=purple):")
    for y in range(h):
        row = "".join(glyph_for(pixels[y * w + x][:3], pixels[y * w + x][3]) for x in range(w))
        lines.append(f"    {y:02d} {row}")
    lines.append("- luma map (dark→light  ░▒▓█):")
    for y in range(h):
        row = "".join(luma_glyph(pixels[y * w + x][:3], pixels[y * w + x][3]) for x in range(w))
        lines.append(f"    {y:02d} {row}")

    # Write 16x nearest-neighbor preview.
    OUT_DIR.mkdir(parents=True, exist_ok=True)
    preview = img.resize((w * SCALE, h * SCALE), Image.Resampling.NEAREST)
    dest = OUT_DIR / f"{path.stem}@x{SCALE}.png"
    preview.save(dest)
    lines.append(f"- wrote preview: {dest.relative_to(ROOT).as_posix()}")
    lines.append("")
    return "\n".join(lines)


def main() -> None:
    paths = sorted(TEX_ROOT.rglob("*.png"))
    if not paths:
        raise SystemExit(f"no png under {TEX_ROOT}")
    chunks = [
        "# TRTR texture inspect",
        f"source: {TEX_ROOT.relative_to(ROOT).as_posix()}",
        f"files: {len(paths)}",
        "",
    ]
    pix_by_name: dict[str, list[tuple[int, int, int, int]]] = {}
    means: dict[str, tuple[int, int, int]] = {}
    for p in paths:
        chunks.append(analyze(p))
        img = Image.open(p).convert("RGBA")
        pix = rgba_list(img)
        pix_by_name[p.stem] = pix
        op = [px[:3] for px in pix if px[3] >= 16]
        if op:
            means[p.stem] = (
                round(sum(c[0] for c in op) / len(op)),
                round(sum(c[1] for c in op) / len(op)),
                round(sum(c[2] for c in op) / len(op)),
            )

    names = list(pix_by_name)
    chunks.append("## pairwise pixel MSE (RGB)")
    header = "                " + " ".join(f"{n[:12]:>12s}" for n in names)
    chunks.append(header)
    for a in names:
        cells = []
        for b in names:
            cells.append(f"{mse_rgb(pix_by_name[a], pix_by_name[b]):12.1f}")
        chunks.append(f"{a[:16]:16s} " + " ".join(cells))
    chunks.append("")
    chunks.append("## mean RGB")
    for n, rgb in means.items():
        chunks.append(f"- {n}: #{rgb[0]:02X}{rgb[1]:02X}{rgb[2]:02X}  {rgb}  ~{name_color(rgb)}")
    chunks.append("")

    report = "\n".join(chunks)
    OUT_DIR.mkdir(parents=True, exist_ok=True)
    (OUT_DIR / "report.md").write_text(report, encoding="utf-8")
    print(report)


if __name__ == "__main__":
    main()
