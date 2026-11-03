"""轻量资源审计：标准库即可运行，不启动服务器或客户端。"""
import argparse
import hashlib
import json
import struct
import zlib
from pathlib import Path

parser = argparse.ArgumentParser()
parser.add_argument("--antique", type=Path, required=True)
args = parser.parse_args()
root = Path(__file__).resolve().parents[1]
resources = root / "src/main/resources"
assets = resources / "assets/tconstruct"
source = args.antique / "src/main/resources/assets/tconstruct"


def png(path):
    raw = path.read_bytes()
    assert raw[:8] == b"\x89PNG\r\n\x1a\n", path
    chunks, cursor = {}, 8
    while cursor < len(raw):
        length = struct.unpack(">I", raw[cursor:cursor + 4])[0]
        kind = raw[cursor + 4:cursor + 8]
        data = raw[cursor + 8:cursor + 8 + length]
        crc = struct.unpack(">I", raw[cursor + 8 + length:cursor + 12 + length])[0]
        assert zlib.crc32(kind + data) & 0xFFFFFFFF == crc, path
        chunks[kind] = chunks.get(kind, b"") + data
        cursor += length + 12
    width, height, depth, color, compression, filtering, interlace = struct.unpack(">IIBBBBB", chunks[b"IHDR"])
    assert not interlace, (path, "interlaced PNG")
    channels = {0: 1, 2: 3, 3: 1, 4: 2, 6: 4}[color]
    bpp = max(1, (channels * depth + 7) // 8)
    stride = (width * channels * depth + 7) // 8
    packed = zlib.decompress(chunks[b"IDAT"])
    previous = bytearray(stride)
    cursor = 0
    rows = []
    for _ in range(height):
        filter_type = packed[cursor]
        row = bytearray(packed[cursor + 1:cursor + 1 + stride])
        cursor += stride + 1
        for x in range(stride):
            a = row[x - bpp] if x >= bpp else 0
            b = previous[x]
            c = previous[x - bpp] if x >= bpp else 0
            if filter_type == 1:
                row[x] = (row[x] + a) & 255
            elif filter_type == 2:
                row[x] = (row[x] + b) & 255
            elif filter_type == 3:
                row[x] = (row[x] + (a + b) // 2) & 255
            elif filter_type == 4:
                p = a + b - c
                pa, pb, pc = abs(p - a), abs(p - b), abs(p - c)
                row[x] = (row[x] + (a if pa <= pb and pa <= pc else b if pb <= pc else c)) & 255
            else:
                assert filter_type == 0, path
        rows.append(row)
        previous = row
    rgba = []
    palette = chunks.get(b"PLTE", b"")
    transparency = chunks.get(b"tRNS", b"")
    for row in rows:
        for x in range(width):
            if color == 3:
                shift = 8 - depth - x * depth % 8
                index = row[x * depth // 8] >> shift & ((1 << depth) - 1)
                rgba.append((*palette[index * 3:index * 3 + 3], transparency[index] if index < len(transparency) else 255))
            elif color == 6 and depth == 8:
                rgba.append(tuple(row[x * 4:x * 4 + 4]))
            elif color == 2 and depth == 8:
                rgb = tuple(row[x * 3:x * 3 + 3])
                transparent = len(transparency) == 6 and rgb == struct.unpack(">HHH", transparency)
                rgba.append((*rgb, 0 if transparent else 255))
            elif color == 4 and depth == 8:
                value, alpha = row[x * 2:x * 2 + 2]
                rgba.append((value, value, value, alpha))
            elif color == 0 and depth == 8:
                value = row[x]
                rgba.append((value, value, value, 255))
            else:
                raise AssertionError((path, depth, color))
    return width, height, rgba, chunks


manifest = json.loads((root / "scripts/antique_tool_textures.json").read_text(encoding="utf-8"))
changed = 0
for entry in manifest:
    path = assets / entry["output"]
    width, height, pixels, chunks = png(path)
    assert (width, height) == (16, 16), (path, width, height)
    assert any(pixel[3] for pixel in pixels), (path, "empty image")
    assert not ({b"eXIf", b"iCCP", b"tEXt", b"iTXt", b"zTXt", b"tIME"} & chunks.keys()), (path, "unexpected metadata")
    if entry["source"]:
        original = source / entry["source"]
        assert hashlib.sha256(original.read_bytes()).hexdigest() == entry["source_sha256"], original
        sw, sh, before, _ = png(original)
        assert (sw, sh) == (width, height), original
        dx, dy = entry.get("offset", [0, 0])
        alpha = [0] * (width * height)
        for y in range(height):
            for x in range(width):
                if 0 <= x + dx < width and 0 <= y + dy < height:
                    alpha[(y + dy) * width + x + dx] = before[y * width + x][3]
        assert alpha == [pixel[3] for pixel in pixels], (path, "silhouette mismatch")
        changed += before != pixels
    if entry["neutral"]:
        assert all(not p[3] or p[0] == p[1] == p[2] for p in pixels), path

json_files = list(resources.rglob("*.json"))
parsed = {path: json.loads(path.read_text(encoding="utf-8-sig")) for path in json_files}
virtual = set()
for path, content in parsed.items():
    if "atlases" in path.parts:
        virtual.update(row["sprite"] for row in content.get("sources", []) if "sprite" in row)
for path, content in parsed.items():
    if "models" not in path.parts:
        continue
    parent = content.get("parent", "")
    if parent.startswith("tconstruct:"):
        assert (assets / ("models/" + parent.split(":", 1)[1] + ".json")).is_file(), (path, parent)
    for texture in content.get("textures", {}).values():
        if texture.startswith("tconstruct:") and texture not in virtual:
            assert (assets / ("textures/" + texture.split(":", 1)[1] + ".png")).is_file(), (path, texture)
    for override in content.get("overrides", []):
        model = override.get("model", "")
        if model.startswith("tconstruct:"):
            assert (assets / ("models/" + model.split(":", 1)[1] + ".json")).is_file(), (path, model)

zh = parsed[assets / "lang/zh_cn.json"]
en = parsed[assets / "lang/en_us.json"]
assert zh.keys() == en.keys(), ("language key mismatch", zh.keys() - en.keys(), en.keys() - zh.keys())
for key in zh:
    assert zh[key].count("%s") == en[key].count("%s"), ("placeholder mismatch", key)
for key, value in zh.items():
    if key.startswith(("modifier.tconstruct.", "item.tconstruct.parts.", "item.tconstruct.tools.")) or (key.startswith("traits.") and key.endswith(".name")):
        assert any("\u4e00" <= c <= "\u9fff" for c in value), ("missing Chinese name", key, value)

print(f"PASS: {len(manifest)} PNGs, alpha silhouettes and original hashes; {changed} changed raster images")
print(f"PASS: {len(json_files)} JSON files, local model/texture references, {len(zh)} bilingual keys")
