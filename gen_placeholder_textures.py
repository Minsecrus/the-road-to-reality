"""生成 trtr 占位贴图（16x16 带噪点）与物品模型 JSON。正式美术到位后整体替换。"""
import json, random, struct, zlib, os
from pathlib import Path

ROOT = Path(__file__).parent / "src/main/resources/assets/trtr"
TEX = ROOT / "textures"
MODELS = ROOT / "models/item"

# (路径, 基色RGB, 名称)
TEXTURES = {
    "item/flint_shard":  (59, 59, 64),
    "item/wood_stick":   (138, 106, 61),
    "item/plant_fiber":  (122, 154, 74),
    "item/flint_axe":    (74, 74, 82),
    "item/flint_knife":  (74, 74, 82),
    "item/flint_shovel": (74, 74, 82),
    "item/flint_hoe":    (74, 74, 82),
    "item/crude_stone_pickaxe": (122, 122, 118),
    "block/loose_stone": (143, 138, 128),
}

HANDHELD = {"flint_axe", "flint_knife", "flint_shovel", "flint_hoe", "crude_stone_pickaxe"}

def png(path, rgb, seed):
    rng = random.Random(seed)
    w = h = 16
    rows = []
    for y in range(h):
        row = bytearray([0])
        for x in range(w):
            n = rng.randint(-22, 22)
            row += bytes(max(0, min(255, c + n)) for c in rgb)
            row.append(255)
        rows.append(bytes(row))
    raw = b"".join(rows)
    def chunk(t, d):
        c = t + d
        return struct.pack(">I", len(d)) + c + struct.pack(">I", zlib.crc32(c))
    data = (b"\x89PNG\r\n\x1a\n"
            + chunk(b"IHDR", struct.pack(">IIBBBBB", w, h, 8, 6, 0, 0, 0))
            + chunk(b"IDAT", zlib.compress(raw, 9))
            + chunk(b"IEND", b""))
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_bytes(data)

for i, (rel, rgb) in enumerate(TEXTURES.items()):
    png(TEX / f"{rel}.png", rgb, seed=i)

# 物品模型
def item_model(name):
    parent = "minecraft:item/handheld" if name in HANDHELD else "minecraft:item/generated"
    return {"parent": parent, "textures": {"layer0": f"trtr:item/{name}"}}

for name in ["flint_shard", "wood_stick", "plant_fiber", "flint_axe", "flint_knife",
             "flint_shovel", "flint_hoe", "crude_stone_pickaxe"]:
    (MODELS / f"{name}.json").write_text(json.dumps(item_model(name), indent=2), encoding="utf-8")

print("done:", len(TEXTURES), "textures,", 8, "item models")
