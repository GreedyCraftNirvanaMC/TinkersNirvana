"""Antique fancy-frame models and recipes, retaining native map-frame geometry."""
import argparse
import hashlib
import json
from pathlib import Path

parser = argparse.ArgumentParser()
parser.add_argument("--antique", type=Path, required=True)
args = parser.parse_args()
project = Path(__file__).resolve().parents[1]
root = project / "src/main/resources"
assets = root / "assets/tconstruct"
old = args.antique / "src/main/resources/assets/tconstruct"
manifest, names = [], {}


def write(path, value):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(value, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")


def texture(name, output):
    source = f"textures/blocks/{name}.png"
    manifest.append({"source": source, "output": "textures/block/gadgets/" + output + ".png", "source_sha256": hashlib.sha256((old / source).read_bytes()).hexdigest(), "neutral": False})
    return "tconstruct:block/gadgets/" + output


background = texture("itemframe_background", "frame_background")
jewel = texture("block_silky_jewel", "frame_jewel")
style = json.loads((old / "models/block/fancy_frame.json").read_text())
types = [("jewel", "宝石", "Jewelled"), ("alubrass", "铝黄铜", "Aluminum Brass"), ("cobalt", "钴", "Cobalt"), ("ardite", "阿迪特", "Ardite"), ("manyullyn", "玛玉灵", "Manyullyn"), ("gold", "金", "Golden"), ("clear", "透明", "Clear")]
tags = {"nuggetGold": "c:nuggets/gold", "paneGlass": "c:glass_panes", "blockGlass": "c:glass_blocks"}
for material in ["Alubrass", "Cobalt", "Ardite", "Manyullyn"]:
    tags["nugget" + material] = "c:nuggets/" + material.lower()
for type_, zh, en in types:
    wood = jewel if type_ == "jewel" else "minecraft:block/gold_block" if type_ == "gold" else "minecraft:block/white_stained_glass" if type_ == "clear" else "tconstruct:block/block_" + type_
    textures = {"wood": wood, "back": "minecraft:block/glass" if type_ == "clear" else background, "particle": "#wood"}
    for map_ in [False, True]:
        write(assets / f"models/block/fancy_frame/{type_}{'_map' if map_ else ''}.json", {"parent": "minecraft:block/item_frame_map" if map_ else "minecraft:block/item_frame", "textures": textures})
    model = {"parent": "tconstruct:block/fancy_frame/" + type_, "display": style["display"]}
    if type_ == "clear":
        model["render_type"] = "minecraft:translucent"
    write(assets / f"models/item/fancy_frame_{type_}.json", model)
    source = "golden" if type_ == "gold" else type_
    recipe = json.loads((old / f"recipes/gadgets/frames/{source}_fancy_item_frame.json").read_text())
    key = {}
    for char, ingredient in recipe["key"].items():
        key[char] = {"tag": tags[ingredient["ore"]]} if "ore" in ingredient else {"item": ingredient["item"]}
    write(root / f"data/tconstruct/recipe/gadgets/frames/{type_}.json", {"type": "minecraft:crafting_shaped", "group": "tconstruct:fancy_frame", "pattern": recipe["pattern"], "key": key, "result": {"id": "tconstruct:fancy_frame_" + type_}})
    names["item.tconstruct.fancy_frame_" + type_] = [zh + "物品展示框", en + " Item Frame"]
write(assets / "models/block/fancy_frame/empty.json", {"parent": "minecraft:block/block", "elements": []})
names["entity.tconstruct.fancy_frame"] = ["精美物品展示框", "Fancy Item Frame"]
for lang, column in [("zh_cn", 0), ("en_us", 1)]:
    target = assets / f"lang/{lang}.json"
    data = json.loads(target.read_text(encoding="utf-8-sig"))
    data.update({key: values[column] for key, values in names.items()})
    write(target, data)
write(project / "scripts/antique_frame_textures.json", manifest)
print("Prepared seven fancy-frame types and two GIMP textures")
