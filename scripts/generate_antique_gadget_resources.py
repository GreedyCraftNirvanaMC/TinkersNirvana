"""Antique's rack, dried clay, jerky, drops, and native drying recipes; raster work runs in GIMP."""
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


def write(path, data):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(data, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")


def texture(source, output):
    manifest.append({"source": "textures/" + source + ".png", "output": "textures/" + output + ".png", "source_sha256": hashlib.sha256((old / ("textures/" + source + ".png")).read_bytes()).hexdigest(), "neutral": False})
    return "tconstruct:" + output


def model_item(name, tex):
    write(assets / f"models/item/{name}.json", {"parent": "minecraft:item/generated", "textures": {"layer0": tex}})


def loot(name, slab=False):
    entry = {"type": "minecraft:item", "name": "tconstruct:" + name}
    if slab:
        entry["functions"] = [{"function": "minecraft:set_count", "count": 2, "conditions": [{"condition": "minecraft:block_state_property", "block": "tconstruct:" + name, "properties": {"type": "double"}}]}]
    write(root / f"data/tconstruct/loot_table/blocks/{name}.json", {"type": "minecraft:block", "pools": [{"rolls": 1, "entries": [entry], "conditions": [{"condition": "minecraft:survives_explosion"}]}]})


def shaped(name, pattern, ingredient, result, count=1):
    write(root / f"data/tconstruct/recipe/gadgets/remaining/{name}.json", {"type": "minecraft:crafting_shaped", "pattern": pattern, "key": {"A": {"item": ingredient}}, "result": {"id": "tconstruct:" + result, "count": count}})


for model in ["rack", "rack_down", "rack_side"]:
    data = json.loads((old / f"models/block/{model}.json").read_text())
    data["parent"] = "minecraft:block/block"
    data["textures"]["leg"] = "minecraft:block/oak_planks"
    write(assets / f"models/block/{model}.json", data)
for name, zh, en, cost in [("item_rack", "物品架", "Item Rack", 2), ("drying_rack", "晾干架", "Drying Rack", 3)]:
    variants = {}
    for face, model in [("floor", "rack_down"), ("ceiling", "rack"), ("wall", "rack_side")]:
        for direction, rotation in [("north", 0), ("east", 90), ("south", 180), ("west", 270)]:
            variants[f"face={face},facing={direction}"] = {"model": "tconstruct:block/" + model, "y": rotation}
    write(assets / f"blockstates/{name}.json", {"variants": variants})
    write(assets / f"models/item/{name}.json", {"parent": "tconstruct:block/rack"})
    names["block.tconstruct." + name] = [zh, en]
    loot(name)
    ingredient = [{"tag": "minecraft:wooden_slabs"}, {"item": "tconstruct:lavawood_slab"}, {"item": "tconstruct:firewood_slab"}]
    write(root / f"data/tconstruct/recipe/gadgets/remaining/{name}.json", {"type": "tconstruct:table", "pattern": ["A" * cost], "key": {"A": ingredient}, "material": ingredient, "result": {"id": "tconstruct:" + name}})

for name, zh, en in [("dried_clay", "干燥黏土", "Dried Clay"), ("dried_clay_bricks", "干燥黏土砖", "Dried Clay Bricks")]:
    tex = texture("blocks/" + name, "block/gadgets/" + name)
    write(assets / f"models/block/{name}.json", {"parent": "minecraft:block/cube_all", "textures": {"all": tex}})
    write(assets / f"blockstates/{name}.json", {"variants": {"": {"model": "tconstruct:block/" + name}}})
    write(assets / f"models/item/{name}.json", {"parent": "tconstruct:block/" + name})
    names["block.tconstruct." + name] = [zh, en]
    loot(name)
    for suffix, parent in [("slab", "slab"), ("slab_top", "slab_top"), ("stairs", "stairs"), ("stairs_inner", "inner_stairs"), ("stairs_outer", "outer_stairs")]:
        write(assets / f"models/block/{name}_{suffix}.json", {"parent": "minecraft:block/" + parent, "textures": {"bottom": tex, "side": tex, "top": tex}})
    for shape, szh, sen, pattern, count in [("slab", "台阶", "Slab", ["AAA"], 6), ("stairs", "楼梯", "Stairs", ["A  ", "AA ", "AAA"], 4)]:
        identifier = name + "_" + shape
        reference = assets / f"blockstates/firewood_{shape}.json"
        write(assets / f"blockstates/{identifier}.json", json.loads(reference.read_text().replace("firewood", name)))
        write(assets / f"models/item/{identifier}.json", {"parent": "tconstruct:block/" + identifier})
        loot(identifier, shape == "slab")
        names["block.tconstruct." + identifier] = [zh + szh, en + " " + sen]
        shaped(identifier, pattern, "tconstruct:" + name, identifier, count)
shaped("dried_bricks", ["AA", "AA"], "tconstruct:dried_brick", "dried_clay_bricks")
shaped("dried_bricks_slab_simple", ["AA"], "tconstruct:dried_brick", "dried_clay_bricks_slab")
model_item("dried_brick", texture("items/materials/dried_brick", "item/gadgets/dried_brick"))
names["item.tconstruct.dried_brick"] = ["干燥黏土砖块", "Dried Clay Brick"]

meats = [("monster", "怪物", "Monster", "rotten_flesh"), ("beef", "牛肉", "Beef", "beef"), ("chicken", "鸡肉", "Chicken", "chicken"), ("pork", "猪肉", "Pork", "porkchop"), ("mutton", "羊肉", "Mutton", "mutton"), ("rabbit", "兔肉", "Rabbit", "rabbit"), ("fish", "鳕鱼", "Fish", "cod"), ("salmon", "鲑鱼", "Salmon", "salmon"), ("clownfish", "热带鱼", "Clownfish", "tropical_fish"), ("pufferfish", "河豚", "Pufferfish", "pufferfish")]


def drying(name, ingredient, result, time, leather=False):
    write(root / f"data/tconstruct/recipe/drying/{name}.json", {"type": "tconstruct:drying", "ingredient": ingredient, "result": {"id": result}, "time": time, **({"leather": True} if leather else {})})


for meat, zh, en, raw in meats:
    name = "jerky_" + meat
    model_item(name, texture("items/food/" + name, "item/gadgets/" + name))
    names["item.tconstruct." + name] = [zh + "干", en + " Jerky"]
    drying(name, {"item": "minecraft:" + raw}, "tconstruct:" + name, 6000)
for color, zh, en in [("green", "绿色", "Green"), ("blue", "蓝色", "Blue"), ("purple", "紫色", "Purple"), ("blood", "血液", "Blood"), ("magma", "岩浆", "Magma")]:
    name = "slimedrop_" + color
    model_item(name, texture("items/food/" + name, "item/gadgets/" + name))
    names["item.tconstruct." + name] = [zh + "史莱姆凝露", en + " Slimedrop"]
    drying(name, {"item": "minecraft:slime_ball" if color == "green" else "tconstruct:slimeball_" + color}, "tconstruct:" + name, 6000)
for meat in ["beef", "chicken", "cod", "salmon", "mutton", "porkchop", "rabbit"]:
    drying("leather_" + meat, {"item": "minecraft:cooked_" + meat}, "minecraft:leather", 10200, leather=True)
drying("clay_ball", {"item": "minecraft:clay_ball"}, "tconstruct:dried_brick", 2400)
drying("clay", {"item": "minecraft:clay"}, "tconstruct:dried_clay", 7200)
drying("sponge", {"item": "minecraft:wet_sponge"}, "minecraft:sponge", 2400)
drying("saplings", {"tag": "minecraft:saplings"}, "minecraft:dead_bush", 7200)
for tag, values in [("mineable/pickaxe", ["tconstruct:" + name + suffix for name in ["dried_clay", "dried_clay_bricks"] for suffix in ["", "_slab", "_stairs"]]), ("mineable/axe", ["tconstruct:item_rack", "tconstruct:drying_rack"])]:
    target = root / f"data/minecraft/tags/block/{tag}.json"
    data = json.loads(target.read_text()) if target.exists() else {"replace": False, "values": []}
    data["values"] = list(dict.fromkeys(data["values"] + values))
    write(target, data)
for lang, column in [("zh_cn", 0), ("en_us", 1)]:
    target = assets / f"lang/{lang}.json"
    data = json.loads(target.read_text(encoding="utf-8-sig"))
    data.update({key: value[column] for key, value in names.items()})
    write(target, data)
write(project / "scripts/antique_gadget_textures.json", manifest)
print(f"Prepared racks, dried clay, drying recipes, and {len(manifest)} GIMP textures")
