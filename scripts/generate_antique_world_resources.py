"""World block states, original loot, configured features, and a GIMP texture manifest."""
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
manifest = []


def write(path, value):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(value, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")


def texture(name, neutral=False, colormap=False):
    source = ("textures/colormap/" if colormap else "textures/blocks/slime/") + name + ".png"
    output = ("textures/colormap/" if colormap else "textures/block/world/") + name + ".png"
    if not any(row["output"] == output for row in manifest):
        manifest.append({"source": source, "output": output, "source_sha256": hashlib.sha256((old / source).read_bytes()).hexdigest(), "neutral": neutral,
                         **({"preserve_pixels": True} if colormap else {})})
    return "tconstruct:" + output.removeprefix("textures/").removesuffix(".png")


names = {}
blocks = {}
tags = {}
silk = {"condition": "minecraft:match_tool", "predicate": {"predicates": {"minecraft:enchantments": [{"enchantments": "minecraft:silk_touch", "levels": {"min": 1}}]}}}
shears = {"condition": "minecraft:match_tool", "predicate": {"items": "minecraft:shears"}}
harvest = {"condition": "minecraft:any_of", "terms": [silk, shears]}
explosion = {"condition": "minecraft:survives_explosion"}


def item(name):
    return {"type": "minecraft:item", "name": name if ":" in name else "tconstruct:" + name}


def pool(entries, conditions=None):
    return {"rolls": 1, "conditions": conditions or [explosion], "entries": entries}


def register(name, model, zh, en, loot=None, variants=None, block_tags=()):
    blocks[name] = model
    write(assets / f"models/block/{name}.json", model)
    write(assets / f"blockstates/{name}.json", variants or {"variants": {"": {"model": "tconstruct:block/" + name}}})
    write(assets / f"models/item/{name}.json", {"parent": "tconstruct:block/" + name})
    write(root / f"data/tconstruct/loot_table/blocks/{name}.json", {"type": "minecraft:block", "pools": loot if loot is not None else [pool([item(name)])]})
    names["block.tconstruct." + name] = [zh, en]
    for tag in block_tags:
        tags.setdefault(tag, []).append("tconstruct:" + name)


colors = {"vanilla": ["普通", "Earth"], "green": ["绿色", "Green"], "blue": ["蓝色", "Blue"], "purple": ["紫色", "Purple"], "magma": ["岩浆", "Magma"], "orange": ["橙色", "Orange"]}
for dirt in ["green", "blue", "purple", "magma"]:
    name = "slime_dirt_" + dirt
    register(name, {"parent": "minecraft:block/cube_all", "textures": {"all": texture("slimedirt_" + dirt)}}, colors[dirt][0] + "史莱姆泥土", colors[dirt][1] + " Slimy Dirt", block_tags=("minecraft:mineable/shovel", "tconstruct:slime_soil"))

for foliage in ["blue", "purple", "orange"]:
    for dirt in ["vanilla", "green", "blue", "purple", "magma"]:
        name = "slime_grass_" + foliage + "_" + dirt
        dirt_texture = "minecraft:block/dirt" if dirt == "vanilla" else texture("slimedirt_" + dirt)
        dirt_item = "minecraft:dirt" if dirt == "vanilla" else "tconstruct:slime_dirt_" + dirt
        model = {"parent": "minecraft:block/grass_block", "textures": {"particle": dirt_texture, "bottom": dirt_texture, "side": dirt_texture,
            "top": texture("slimegrass_top", True), "overlay": texture("slimegrass_overlay", True)}}
        variant = {"variants": {"snowy=false": {"model": "tconstruct:block/" + name}, "snowy=true": {"model": "tconstruct:block/" + name + "_snow"}}}
        register(name, model, colors[foliage][0] + "史莱姆草方块（" + colors[dirt][0] + "泥土）", colors[foliage][1] + " Slimy Grass (" + colors[dirt][1] + " Dirt)",
            [pool([{"type": "minecraft:alternatives", "children": [{**item(name), "conditions": [silk]}, item(dirt_item)]}])], variant,
            ("minecraft:mineable/shovel", "tconstruct:slime_soil"))
        tags.setdefault("tconstruct:slime_spawnable/" + ("purple" if dirt == "purple" else "blue"), []).append("tconstruct:" + name)
        snow = json.loads(json.dumps(model))
        snow["textures"]["overlay"] = texture("slimesnow_overlay", True)
        write(assets / f"models/block/{name}_snow.json", snow)
    name = "slime_leaves_" + foliage
    drops = [pool([{**item(name), "conditions": [harvest]}])]
    drops.append(pool([item("slime_sapling_" + foliage)], [explosion, {"condition": "minecraft:inverted", "term": harvest},
        {"condition": "minecraft:table_bonus", "enchantment": "minecraft:fortune", "chances": [.04, .04444445, .05, .06666667]}]))
    if foliage != "orange":
        slime_drop = item("slimeball_purple") if foliage == "purple" else {"type": "minecraft:alternatives", "children": [
            {**item("slimeball_blue"), "conditions": [{"condition": "minecraft:random_chance", "chance": 1 / 3}]}, item("minecraft:slime_ball")]}
        drops.append(pool([slime_drop], [explosion, {"condition": "minecraft:inverted", "term": harvest}, {"condition": "minecraft:table_bonus", "enchantment": "minecraft:fortune", "chances": [.005, .00555556, .00625, .008333334]}]))
    register(name, {"parent": "minecraft:block/leaves", "textures": {"all": texture("slimeleaves", True)}}, colors[foliage][0] + "史莱姆树叶", colors[foliage][1] + " Slimy Leaves", drops, block_tags=("minecraft:leaves", "minecraft:mineable/hoe"))
    for type_, source, zh, en in [("tall_grass", "slimegrass_tall", "史莱姆草", "Slimy Grass"), ("fern", "slimegrass_fern", "史莱姆蕨", "Slimy Fern")]:
        name = "slime_" + type_ + "_" + foliage
        register(name, {"parent": "minecraft:block/tinted_cross", "textures": {"cross": texture(source, True)}}, colors[foliage][0] + zh, colors[foliage][1] + " " + en,
            [pool([{**item(name), "conditions": [shears]}])], block_tags=("minecraft:replaceable", "tconstruct:slime_plants"))
    name = "slime_sapling_" + foliage
    register(name, {"parent": "minecraft:block/cross", "textures": {"cross": texture("slimesapling_" + foliage)}}, colors[foliage][0] + "史莱姆树苗", colors[foliage][1] + " Slime Sapling", block_tags=("minecraft:saplings", "tconstruct:slime_plants"))
    write(root / f"data/tconstruct/worldgen/configured_feature/slime_tree_{foliage}.json", {"type": "tconstruct:slime_tree", "config": {
        "trunk": {"Name": "tconstruct:slime_congealed_" + ("magma" if foliage == "orange" else "green")},
        "leaves": {"Name": "tconstruct:slime_leaves_" + foliage}, "seek_height": False}})

for foliage in ["blue", "purple"]:
    for stage, suffix in enumerate(["", "_mid", "_end"]):
        name = "slime_vine_" + foliage + suffix
        variant = {"multipart": [{"when": {direction: "true"}, "apply": {"model": "tconstruct:block/" + name, "y": rotation, "uvlock": True}}
            for direction, rotation in [("north", 0), ("east", 90), ("south", 180), ("west", 270)]]}
        register(name, {"parent": "minecraft:block/vine", "textures": {"vine": texture("slimevine" + suffix, True), "particle": texture("slimevine" + suffix, True)}},
            colors[foliage][0] + "史莱姆藤蔓" + ["", "中段", "末端"][stage], colors[foliage][1] + " Slime Vine" + ["", " Middle", " End"][stage],
            [pool([{**item(name), "conditions": [shears]}])], variant, ("minecraft:climbable", "minecraft:replaceable", "tconstruct:slime_plants"))
        write(assets / f"models/item/{name}.json", {"parent": "minecraft:item/generated", "textures": {"layer0": texture("slimevine" + suffix, True)}})

for name in ["slimegrasscolor", "purplegrasscolor", "orangegrasscolor"]:
    texture(name, colormap=True)
for color in ["blue", "purple"]:
    # Regenerate the source-defined list; the spawn rule depends on dirt, not foliage.
    write(root / f"data/tconstruct/tags/block/slime_spawnable/{color}.json", {"replace": False, "values": tags.pop("tconstruct:slime_spawnable/" + color)})

for name, biome in [("slime_island", "#minecraft:is_overworld"), ("magma_island", "#minecraft:is_nether"), ("slime_pool", "#minecraft:is_overworld"), ("slime_pool_humid", "#minecraft:is_overworld"), ("magma_pool", "#minecraft:is_nether")]:
    feature = "slime_pool" if name.endswith("humid") else name
    write(root / f"data/tconstruct/worldgen/configured_feature/{name}.json", {"type": "tconstruct:" + feature, "config": {}})
    placement = [{"type": "tconstruct:antique_worldgen", "setting": name}]
    if "pool" in name:
        placement.append({"type": "tconstruct:antique_height", "setting": name})
    write(root / f"data/tconstruct/worldgen/placed_feature/{name}.json", {"feature": "tconstruct:" + name, "placement": placement})
    write(root / f"data/tconstruct/neoforge/biome_modifier/{name}.json", {"type": "neoforge:add_features", "biomes": biome, "features": "tconstruct:" + name,
        "step": "vegetal_decoration" if "island" in name else "lakes"})

for metal in ["cobalt", "ardite", "copper", "tin", "aluminum"]:
    nether = metal in ["cobalt", "ardite"]
    target = {"predicate_type": "minecraft:block_match", "block": "minecraft:netherrack" if nether else "minecraft:stone"}
    name = "ore_" + metal
    write(root / f"data/tconstruct/worldgen/configured_feature/{name}.json", {"type": "minecraft:ore", "config": {"size": 5, "discard_chance_on_air_exposure": 0,
        "targets": [{"target": target, "state": {"Name": "tconstruct:" + metal + "_ore"}}]}})
    write(root / f"data/tconstruct/worldgen/placed_feature/{name}.json", {"feature": "tconstruct:" + name, "placement": [{"type": "tconstruct:antique_height", "setting": metal}, {"type": "minecraft:biome"}]})
    write(root / f"data/tconstruct/neoforge/biome_modifier/{name}.json", {"type": "neoforge:add_features", "biomes": "#minecraft:is_nether" if nether else "#minecraft:is_overworld",
        "features": "tconstruct:" + name, "step": "underground_ores"})
for tag, values in tags.items():
    namespace, path = tag.split(":")
    target = root / f"data/{namespace}/tags/block/{path}.json"
    data = json.loads(target.read_text()) if target.exists() else {"replace": False, "values": []}
    data["values"] = list(dict.fromkeys(data["values"] + values))
    write(target, data)
for tag, values in [("saplings", ["tconstruct:slime_sapling_" + color for color in ["blue", "purple", "orange"]]), ("leaves", ["tconstruct:slime_leaves_" + color for color in ["blue", "purple", "orange"]])]:
    target = root / f"data/minecraft/tags/item/{tag}.json"
    data = json.loads(target.read_text()) if target.exists() else {"replace": False, "values": []}
    data["values"] = list(dict.fromkeys(data["values"] + values))
    write(target, data)
for lang, col in [("zh_cn", 0), ("en_us", 1)]:
    target = assets / f"lang/{lang}.json"
    data = json.loads(target.read_text(encoding="utf-8-sig"))
    data.update({key: value[col] for key, value in names.items()})
    write(target, data)
write(project / "scripts/antique_world_textures.json", manifest)
print(f"Prepared {len(blocks)} world blocks and {len(manifest)} textures for GIMP")
