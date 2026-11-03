"""Generate the remaining Antique content's models/data and GIMP source manifest."""
import argparse
import hashlib
import json
from pathlib import Path

parser = argparse.ArgumentParser()
parser.add_argument("--antique", type=Path, required=True)
parser.add_argument("--output", type=Path, default=Path(__file__).resolve().parents[1])
args = parser.parse_args()
old = args.antique / "src/main/resources/assets/tconstruct"
root = args.output / "src/main/resources"
assets = root / "assets/tconstruct"
manifest = []


def write(path, data):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(data, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")


def texture(source):
    if source.startswith("#"):
        return source
    if source.startswith("minecraft:"):
        return {"minecraft:blocks/planks_oak": "minecraft:block/oak_planks", "minecraft:blocks/log_oak": "minecraft:block/oak_log"}.get(source, source.replace(":blocks/", ":block/"))
    if source == "tconstruct:blocks/table_side":
        return "tconstruct:block/table_side"
    relative = source.removeprefix("tconstruct:blocks/")
    source_file = "textures/blocks/" + relative + ".png"
    output_file = "textures/block/workshop/" + relative + ".png"
    if not any(row["output"] == output_file for row in manifest):
        manifest.append({"source": source_file, "source_sha256": hashlib.sha256((old / source_file).read_bytes()).hexdigest(),
                         "output": output_file, "neutral": False})
    return "tconstruct:block/workshop/" + relative


names = {"crafting_station": "craftingstation", "stencil_table": "stenciltable", "part_builder": "partbuilder"}
def variants(name, casts=False):
    output = {}
    for direction, rotation in [("north", 0), ("east", 90), ("south", 180), ("west", 270)]:
        for cast in ([False, True] if casts else [None]):
            key = "facing=" + direction + (",casts=" + str(cast).lower() if cast is not None else "")
            model = "cast_chest" if cast and name == "pattern_chest" else name
            output[key] = {"model": "tconstruct:block/" + model, "y": rotation}
    return {"variants": output}
for name, original in names.items():
    model = json.loads((old / f"models/block/{original}.json").read_text())
    model["textures"] = {("leg_bottom" if key == "legBottom" else key): texture(value) for key, value in model["textures"].items()}
    write(assets / f"models/block/{name}.json", model)
    write(assets / f"blockstates/{name}.json", variants(name))

for prefix in ("pattern", "part", "cast"):
    name = prefix + "_chest"
    model = json.loads((old / "models/block/patternchest.json").read_text())
    model["parent"] = "minecraft:block/block"
    model["textures"].update({part: texture(f"tconstruct:blocks/chest/{prefix}_{part}") for part in ["side", "front", "top", "drawer_front", "drawer_side"]})
    write(assets / f"models/block/{name}.json", model)
    if prefix != "cast":
        write(assets / f"blockstates/{name}.json", variants(name, True))

for name in ["toolstation", "toolforge"]:
    write(assets / f"blockstates/{name}.json", variants(name))

for name in [*names, "pattern_chest", "part_chest"]:
    write(assets / f"models/item/{name}.json", {"parent": f"tconstruct:block/{name}"})
    write(root / f"data/tconstruct/loot_table/blocks/{name}.json", {"type": "minecraft:block", "pools": [{"rolls": 1,
        "entries": [{"type": "minecraft:item", "name": f"tconstruct:{name}"}], "conditions": [{"condition": "minecraft:survives_explosion"}]}]})

tag_file = root / "data/minecraft/tags/block/mineable/axe.json"
tag = json.loads(tag_file.read_text()) if tag_file.exists() else {"replace": False, "values": []}
for name in [*names, "pattern_chest", "part_chest", "toolstation"]:
    if "tconstruct:" + name not in tag["values"]:
        tag["values"].append("tconstruct:" + name)
write(tag_file, tag)


def shaped(name, pattern, key, material=None):
    recipe = {"type": "tconstruct:table" if material else "minecraft:crafting_shaped", "pattern": pattern,
        "key": {symbol: ({"tag": item[1:]} if item.startswith("#") else {"item": item}) for symbol, item in key.items()},
        "result": {"id": "tconstruct:" + name}}
    if material:
        recipe["material"] = recipe["key"][material]
    write(root / f"data/tconstruct/recipe/workshop/{name}.json", recipe)


write(root / "data/tconstruct/recipe/workshop/crafting_station.json", {"type": "minecraft:crafting_shapeless", "ingredients": [{"item": "minecraft:crafting_table"}], "result": {"id": "tconstruct:crafting_station"}})
shaped("stencil_table", ["P", "W"], {"P": "tconstruct:pattern", "W": "#minecraft:planks"}, "W")
shaped("part_builder", ["P", "W"], {"P": "tconstruct:pattern", "W": "#minecraft:logs"}, "W")
shaped("pattern_chest", ["P", "C"], {"P": "tconstruct:pattern", "C": "#c:chests/wooden"})
shaped("part_chest", [" P ", "SCS", " W "], {"P": "tconstruct:pattern", "S": "#c:rods/wooden", "C": "#c:chests/wooden", "W": "#minecraft:planks"})
write(root / "data/tconstruct/recipe/workshop/pattern_chest_planks.json", {"type": "minecraft:crafting_shaped", "pattern": ["WWW", "WPW", "WWW"],
    "key": {"P": {"item": "tconstruct:pattern"}, "W": {"tag": "minecraft:planks"}}, "result": {"id": "tconstruct:pattern_chest"}})

for metal in ["iron", "gold", "cobalt", "ardite", "manyullyn", "pigiron", "knightslime", "copper", "bronze", "alubrass", "steel", "silver", "electrum", "lead"]:
    path = root / ("data/tconstruct/recipe/tools/toolforge.json" if metal == "iron" else f"data/tconstruct/recipe/workshop/toolforge_{metal}.json")
    ingredient = {"tag": "c:storage_blocks/" + metal}
    write(path, {"type": "tconstruct:table", "group": "tconstruct:tool_forge", "pattern": ["BBB", "MTM", "M M"],
        "key": {"B": {"item": "tconstruct:seared_bricks"}, "M": ingredient, "T": {"item": "tconstruct:blockitem/toolstation"}},
        "material": ingredient, "result": {"id": "tconstruct:toolforge"}})

translations = {
    "block.tconstruct.crafting_station": ["合成站", "Crafting Station"],
    "block.tconstruct.stencil_table": ["模具加工台", "Stencil Table"],
    "block.tconstruct.part_builder": ["部件加工台", "Part Builder"],
    "block.tconstruct.pattern_chest": ["模具箱", "Pattern Chest"],
    "block.tconstruct.cast_chest": ["铸模箱", "Cast Chest"],
    "block.tconstruct.part_chest": ["部件箱", "Part Chest"],
    "gui.tconstruct.tool_name": ["工具名称", "Tool name"],
}
for lang, column in [("zh_cn", 0), ("en_us", 1)]:
    path = assets / f"lang/{lang}.json"
    data = json.loads(path.read_text(encoding="utf-8-sig"))
    data.update({key: values[column] for key, values in translations.items()})
    write(path, data)
write(args.output / "scripts/antique_remaining_textures.json", manifest)
print(f"Prepared remaining-content resources and {len(manifest)} GIMP textures")
