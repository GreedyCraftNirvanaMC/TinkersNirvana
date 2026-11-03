"""Antique's hidden spaghetti food chain and seasoning model overlays."""
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


for name in ["spaghetti_hard", "spaghetti_soggy", "spaghetti_cold", "moms_spaghetti", "spaghetti_sauce", "spaghetti_meat"]:
    source = f"textures/items/food/{name}.png"
    manifest.append({"source": source, "output": f"textures/item/gadgets/{name}.png", "source_sha256": hashlib.sha256((old / source).read_bytes()).hexdigest(), "neutral": False})
    if name not in ["spaghetti_sauce", "spaghetti_meat"]:
        model = {"parent": "minecraft:item/generated", "textures": {"layer0": "tconstruct:item/gadgets/" + name}}
        if name == "moms_spaghetti":
            model["overrides"] = [{"predicate": {"tconstruct:seasoning": value}, "model": f"tconstruct:item/moms_spaghetti_{variant}"} for value, variant in [(1, "sauce"), (2, "meat")]]
        write(assets / f"models/item/{name}.json", model)
for seasoning in ["sauce", "meat"]:
    textures = {"layer0": "tconstruct:item/gadgets/moms_spaghetti", "layer1": "tconstruct:item/gadgets/spaghetti_sauce"}
    if seasoning == "meat":
        textures["layer2"] = "tconstruct:item/gadgets/spaghetti_meat"
    write(assets / f"models/item/moms_spaghetti_{seasoning}.json", {"parent": "minecraft:item/generated", "textures": textures})
write(root / "data/tconstruct/recipe/drying/spaghetti.json", {"type": "tconstruct:drying", "ingredient": {"item": "tconstruct:spaghetti_soggy"}, "result": {"id": "tconstruct:spaghetti_cold"}, "time": 18000})
write(root / "data/tconstruct/recipe/gadgets/moms_spaghetti.json", {"type": "minecraft:smelting", "ingredient": {"item": "tconstruct:spaghetti_cold"}, "result": {"id": "tconstruct:moms_spaghetti"}, "experience": 2, "cookingtime": 200})
write(root / "data/c/tags/item/foods/cooked_meat.json", {"replace": False, "values": ["minecraft:cooked_" + name for name in ["beef", "chicken", "mutton", "porkchop", "rabbit"]]})
for name, zh, en in [("hard", "硬意面", "Hard Spaghetti"), ("soggy", "湿意面", "Soggy Spaghetti"), ("cold", "凉意面", "Cold Spaghetti")]:
    names["item.tconstruct.spaghetti_" + name] = [zh, en]
names.update({
    "item.tconstruct.moms_spaghetti": ["妈妈的意面", "Mom's Spaghetti"],
    "modifier.tconstruct.spaghetti_sauce": ["意面酱汁", "Spaghetti Sauce"],
    "modifier.tconstruct.spaghetti_meat": ["意面肉料", "Spaghetti Meat"],
    "tooltip.tconstruct.spaghetti.uses": ["剩余食用次数：%s / %s", "Uses remaining: %s / %s"],
    "tooltip.tconstruct.spaghetti": ["可在工具装配台中用小麦补充次数；先添加甜菜汤，再添加三种不同的熟肉。", "Refill with wheat in a tool station. Add beetroot soup, then three different cooked meats."],
})
for lang, column in [("zh_cn", 0), ("en_us", 1)]:
    target = assets / f"lang/{lang}.json"
    data = json.loads(target.read_text(encoding="utf-8-sig"))
    data.update({key: values[column] for key, values in names.items()})
    write(target, data)
write(project / "scripts/antique_spaghetti_textures.json", manifest)
print("Prepared hidden spaghetti chain and six GIMP textures")
