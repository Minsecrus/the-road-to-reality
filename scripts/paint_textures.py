#!/usr/bin/env python3
"""Paint TRTR 16x16 textures as explicit pixel maps (plus one cobble fill).

Run:  python scripts/paint_textures.py
Writes RGBA PNGs under src/main/resources/assets/trtr/textures/.
"""

from __future__ import annotations

from pathlib import Path

from PIL import Image, ImageDraw, ImageFont

ROOT = Path(__file__).resolve().parents[1]
TEX = ROOT / "src" / "main" / "resources" / "assets" / "trtr" / "textures"
ITEM = TEX / "item"
BLOCK = TEX / "block"
PREVIEW = ROOT / "scripts" / "_texture_inspect"

CLEAR = (0, 0, 0, 0)
K = (0, 0, 0, 255)

# Flint: cool dark gray, glassy flake highlight.
FLINT = {
    "k": K,
    "h": (176, 182, 194, 255),
    "l": (112, 118, 132, 255),
    "m": (70, 74, 86, 255),
    "d": (38, 40, 48, 255),
}

# Stick: oak-ish, highlight along the upper edge.
WOOD = {
    "k": K,
    "H": (204, 152, 82, 255),
    "L": (158, 106, 50, 255),
    "M": (116, 74, 30, 255),
    "D": (74, 46, 16, 255),
}

# Dried grass / lashing.
FIBER = {
    "k": K,
    "g": (214, 202, 90, 255),
    "f": (156, 166, 54, 255),
    "e": (110, 126, 38, 255),
    "a": (74, 90, 26, 255),
}

# Warm cobble / loose stone / crude head.
STONE = {
    "k": K,
    "s": (192, 186, 174, 255),
    "t": (160, 154, 142, 255),
    "o": (126, 120, 108, 255),
    "n": (94, 88, 76, 255),
    "c": (58, 54, 46, 255),
}

TOOL = {**FLINT, **WOOD, **FIBER}
PICK = {**STONE, **WOOD, **FIBER}


def rows(*lines: str) -> tuple[str, ...]:
    if len(lines) != 16:
        raise ValueError(f"need 16 rows, got {len(lines)}")
    for i, line in enumerate(lines):
        if len(line) != 16:
            raise ValueError(f"row {i} len={len(line)}: {line!r}")
        if " " in line:
            raise ValueError(f"space in row {i}: {line!r}")
    return lines


def paint(palette: dict[str, tuple[int, int, int, int]], lines: tuple[str, ...]) -> Image.Image:
    img = Image.new("RGBA", (16, 16), CLEAR)
    px = img.load()
    for y, line in enumerate(lines):
        for x, ch in enumerate(line):
            if ch == ".":
                continue
            try:
                px[x, y] = palette[ch]
            except KeyError as e:
                raise KeyError(f"unknown '{ch}' at ({x},{y})") from e
    return img


def dump(name: str, img: Image.Image, palette: dict[str, tuple[int, int, int, int]]) -> None:
    inv: dict[tuple[int, int, int, int], str] = {}
    for ch, rgba in palette.items():
        inv.setdefault(rgba, ch)
    px = img.load()
    print(f"\n## {name}")
    for y in range(16):
        line = []
        for x in range(16):
            p = px[x, y]
            if p[3] == 0:
                line.append(".")
            else:
                line.append(inv.get(p, "?"))
        print("   " + "".join(line))
    opaque = sum(1 for y in range(16) for x in range(16) if px[x, y][3] > 0)
    colors = {(px[x, y][:3]) for y in range(16) for x in range(16) if px[x, y][3] > 0}
    print(f"   opaque={opaque}/256  unique_rgb={len(colors)}")


def save(img: Image.Image, path: Path) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    img.save(path, format="PNG", optimize=True)


def shaded_ellipse(
    img: Image.Image,
    cx: float,
    cy: float,
    rx: float,
    ry: float,
    pal: dict[str, tuple[int, int, int, int]],
) -> None:
    px = img.load()
    for y in range(16):
        for x in range(16):
            dx = (x + 0.5 - cx) / rx
            dy = (y + 0.5 - cy) / ry
            r2 = dx * dx + dy * dy
            if r2 > 1.0:
                continue
            shade = -dx - dy
            if r2 > 0.78:
                c = pal["edge"]
            elif shade > 0.45:
                c = pal["hi"]
            elif shade > 0.05:
                c = pal["lt"]
            elif shade > -0.4:
                c = pal["md"]
            else:
                c = pal["dk"]
            px[x, y] = c


def paint_loose_stone() -> Image.Image:
    grout = STONE["c"]
    img = Image.new("RGBA", (16, 16), grout)
    pale = {
        "hi": STONE["s"],
        "lt": STONE["t"],
        "md": STONE["o"],
        "dk": STONE["n"],
        "edge": STONE["c"],
    }
    dark = {
        "hi": (168, 164, 156, 255),
        "lt": (136, 132, 124, 255),
        "md": (104, 100, 92, 255),
        "dk": (76, 72, 64, 255),
        "edge": (48, 44, 38, 255),
    }
    warm = {
        "hi": (186, 174, 154, 255),
        "lt": (154, 142, 122, 255),
        "md": (122, 110, 92, 255),
        "dk": (92, 80, 64, 255),
        "edge": (56, 48, 38, 255),
    }
    # Large pebbles so the texture still reads on the tiny pile faces.
    shaded_ellipse(img, 4.0, 3.6, 5.4, 4.5, pale)
    shaded_ellipse(img, 11.6, 3.8, 5.1, 4.7, dark)
    shaded_ellipse(img, 3.6, 11.4, 5.0, 5.2, dark)
    shaded_ellipse(img, 12.0, 11.6, 5.3, 4.9, warm)
    shaded_ellipse(img, 8.0, 8.0, 3.8, 3.4, pale)
    px = img.load()
    for x, y in ((7, 1), (8, 7), (1, 8), (14, 8), (7, 14), (9, 9), (6, 10)):
        px[x, y] = (42, 38, 32, 255)
    return img


# ---------------------------------------------------------------------------
# Item maps. k = outline. Flint h/l/m/d, wood H/L/M/D, fiber g/f/e/a,
# stone s/t/o/n. Handle runs bottom-left → head top-right (vanilla handheld).
# ---------------------------------------------------------------------------

FLINT_SHARD = rows(
    "................",
    "......kkk.......",
    ".....khhlk......",
    "....khhlllk.....",
    "...khhllllk.....",
    "..khlllllmk.....",
    "..khllllmmk.....",
    ".kkhlllmmmk.....",
    ".khlllmmmdk.....",
    ".klllmmmmdk.....",
    ".kllmmmmdk......",
    ".klmmmmdk.......",
    ".kmmmmdk........",
    ".kmmmdk.........",
    ".kkmdk..........",
    "..kkk...........",
)

PLANT_FIBER = rows(
    "................",
    "....kk...kk.....",
    "...kgfk.kfgk....",
    "...kfeekfeek....",
    "..kfeeeeeeek....",
    "..kfeeeeeeek....",
    "...kfeeaeek.....",
    "...kfeaaaek.....",
    "...kfeeaeek.....",
    "..kfeeeeeeek....",
    "..kfeeeeeeek....",
    "...kfeekfeek....",
    "...kgfk.kfgk....",
    "....kk...kk.....",
    "................",
    "................",
)

FLINT_AXE = rows(
    "................",
    "........kkkk....",
    "......kkhllmk...",
    "....kkhhllmmk...",
    "...khhlllmmmk...",
    "..khllllmmdk....",
    "..kllllmmdk.....",
    "...kllmmkLMk....",
    "...kmmkfLLMk....",
    "....kkfHLMMk....",
    ".....kHLMMk.....",
    "....kHLMMk......",
    "...kHLMMk.......",
    "..kHLMMk........",
    ".kHLMk..........",
    "kLMkk...........",
)

FLINT_KNIFE = rows(
    "............kk..",
    "...........khlk.",
    "..........khllk.",
    ".........khllmk.",
    "........khlllmk.",
    ".......khlllmmk.",
    "......khlllmdk..",
    ".....khllmmk....",
    "....khlmmk......",
    "...kkfmk........",
    "...kHLMk........",
    "..kHLMk.........",
    ".kHLMk..........",
    "kHMk............",
    "kMk.............",
    "kk..............",
)

FLINT_SHOVEL = rows(
    ".........kkk....",
    "........khlk....",
    ".......khlllk...",
    "......khllllk...",
    ".....khlllmmk...",
    ".....klllmmmk...",
    "......kllmdk....",
    ".......kmdk.....",
    ".......kfMk.....",
    "......kHLMk.....",
    ".....kHLMMk.....",
    "....kHLMMk......",
    "...kHLMMk.......",
    "..kHLMk.........",
    ".kHMk...........",
    "kMkk............",
)

FLINT_HOE = rows(
    ".....kkkkkk.....",
    "....khlmmmdk....",
    "....khllmdk.....",
    ".....klmdk......",
    ".....kfMk.......",
    "....kHLMk.......",
    "...kHLMMk.......",
    "..kHLMMk........",
    ".kHLMMk.........",
    "kHLMMk..........",
    "kHLMk...........",
    "kHMk............",
    "kMk.............",
    "kk..............",
    "................",
    "................",
)

CRUDE_PICK = rows(
    "................",
    "ksttokkknnotk...",
    "ktssooonnnotk...",
    "kttsookknootk...",
    ".kkk.kk.kkk.....",
    ".....kfk........",
    ".....kHMk.......",
    "....kHLMk.......",
    "...kHLMMk.......",
    "..kHLMMk........",
    ".kHLMMk.........",
    "kHLMMk..........",
    "kHLMk...........",
    "kHMk............",
    "kMk.............",
    "kk..............",
)

WOOD_STICK = rows(
    "................",
    "..........kkk...",
    ".........kHLk...",
    "........kHLMk...",
    ".......kHLMk....",
    "......kHLMk.....",
    ".....kHLMk......",
    "....kHLMk.......",
    "...kHLMk........",
    "..kHLMk.........",
    ".kHLMk..........",
    "kHLMk...........",
    "kLMk............",
    "kMk.............",
    "kk..............",
    "................",
)


SPRITES: list[tuple[str, Path, dict, tuple[str, ...] | None]] = [
    ("flint_shard", ITEM / "flint_shard.png", FLINT, FLINT_SHARD),
    ("plant_fiber", ITEM / "plant_fiber.png", FIBER, PLANT_FIBER),
    ("flint_axe", ITEM / "flint_axe.png", TOOL, FLINT_AXE),
    ("flint_knife", ITEM / "flint_knife.png", TOOL, FLINT_KNIFE),
    ("flint_shovel", ITEM / "flint_shovel.png", TOOL, FLINT_SHOVEL),
    ("flint_hoe", ITEM / "flint_hoe.png", TOOL, FLINT_HOE),
    ("crude_stone_pickaxe", ITEM / "crude_stone_pickaxe.png", PICK, CRUDE_PICK),
    ("wood_stick", ITEM / "wood_stick.png", WOOD, WOOD_STICK),
]


def atlas(named: list[tuple[str, Image.Image]]) -> Image.Image:
    scale = 12
    tile = 16 * scale
    pad = 12
    label_h = 18
    cols = 3
    rows_n = (len(named) + cols - 1) // cols
    w = cols * tile + (cols + 1) * pad
    h = rows_n * (tile + label_h) + (rows_n + 1) * pad
    sheet = Image.new("RGBA", (w, h), (36, 34, 32, 255))
    draw = ImageDraw.Draw(sheet)
    try:
        font = ImageFont.truetype(r"C:\Windows\Fonts\consola.ttf", 13)
    except OSError:
        font = ImageFont.load_default()

    chk_a, chk_b = (92, 92, 92, 255), (58, 58, 58, 255)
    for i, (name, img) in enumerate(named):
        col, row = i % cols, i // cols
        x = pad + col * (tile + pad)
        y = pad + row * (tile + label_h + pad)
        for yy in range(0, tile, scale):
            for xx in range(0, tile, scale):
                c = chk_a if ((xx + yy) // scale) % 2 == 0 else chk_b
                draw.rectangle([x + xx, y + yy, x + xx + scale - 1, y + yy + scale - 1], fill=c)
        scaled = img.resize((tile, tile), Image.Resampling.NEAREST)
        sheet.alpha_composite(scaled, (x, y))
        draw.text((x, y + tile + 2), name, fill=(220, 214, 200, 255), font=font)
    return sheet


def main() -> None:
    PREVIEW.mkdir(parents=True, exist_ok=True)
    painted: list[tuple[str, Image.Image]] = []
    for name, path, pal, lines in SPRITES:
        img = paint(pal, lines)
        dump(name, img, pal)
        save(img, path)
        save(img.resize((256, 256), Image.Resampling.NEAREST), PREVIEW / f"{name}@x16.png")
        painted.append((name, img))
        print(f"   wrote {path.relative_to(ROOT).as_posix()}")

    stone = paint_loose_stone()
    loose_pal = {
        **STONE,
        "A": (168, 164, 156, 255),
        "B": (136, 132, 124, 255),
        "C": (104, 100, 92, 255),
        "E": (76, 72, 64, 255),
        "F": (48, 44, 38, 255),
        "G": (186, 174, 154, 255),
        "I": (154, 142, 122, 255),
        "J": (122, 110, 92, 255),
        "N": (92, 80, 64, 255),
        "O": (56, 48, 38, 255),
        "P": (42, 38, 32, 255),
    }
    dump("loose_stone", stone, loose_pal)
    save(stone, BLOCK / "loose_stone.png")
    save(stone.resize((256, 256), Image.Resampling.NEAREST), PREVIEW / "loose_stone@x16.png")
    painted.append(("loose_stone", stone))
    print(f"   wrote {(BLOCK / 'loose_stone.png').relative_to(ROOT).as_posix()}")

    sheet = atlas(painted)
    save(sheet, PREVIEW / "atlas.png")
    print(f"\nwrote atlas { (PREVIEW / 'atlas.png').relative_to(ROOT).as_posix() }")


if __name__ == "__main__":
    main()
