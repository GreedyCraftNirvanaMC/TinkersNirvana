"""从指定 Antique 源码生成工具资源和 GIMP 加工清单；不编辑图像像素。"""
import argparse
import hashlib
import json
import shutil
from pathlib import Path

parser = argparse.ArgumentParser()
parser.add_argument("--antique", type=Path, required=True)
parser.add_argument("--output", type=Path, default=Path(__file__).resolve().parents[1])
args = parser.parse_args()
assets = args.antique / "src/main/resources/assets/tconstruct"
resources = args.output / "src/main/resources"
dest = resources / "assets/tconstruct"
manifest = []


def write(path, content):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(content, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")


def texture(source, target, offset=(0, 0), neutral=True):
    source = source.replace("tconstruct:", "textures/") + ".png"
    path = assets / source
    if not path.is_file():
        raise FileNotFoundError(path)
    relative = "textures/" + target + ".png"
    if not any(row["output"] == relative for row in manifest):
        manifest.append({"source": source, "source_sha256": hashlib.sha256(path.read_bytes()).hexdigest(),
                         "output": relative, "offset": offset, "neutral": neutral})
    return "tconstruct:" + target


parts = {}
for path in sorted((assets / "models/item/parts").glob("*.tmat.json")):
    original_id = path.name.removesuffix(".tmat.json")
    part = "pickaxe_head" if original_id == "pick_head" else original_id
    model = json.loads(path.read_text())
    offset = model.get("offset", {})
    tex = texture(model["textures"]["layer0"], "item/parts/" + part,
                  (offset.get("x", 0), offset.get("y", 0)))
    write(dest / f"models/item/parts/{part}.json", {"parent": "minecraft:item/generated", "textures": {"layer0": tex}})
    parts[part] = {"source": model["textures"]["layer0"], "offset": offset}

# 包覆核心的原始图层与完整弩箭保持一致。
core_layers = {"layer0": texture("tconstruct:items/bolt/shaft", "item/parts/bolt_core"),
               "layer1": texture("tconstruct:items/bolt/head", "item/parts/bolt_core_coating")}
write(dest / "models/item/parts/bolt_core.json", {"parent": "minecraft:item/generated", "textures": core_layers})

tool_ids = "pickaxe shovel hatchet mattock kama hammer excavator lumberaxe scythe broadsword longsword rapier frypan battlesign cleaver shortbow longbow crossbow arrow bolt shuriken".split()
for tool in tool_ids:
    original = json.loads((assets / f"models/item/tools/{tool}.tcon.json").read_text())
    tool_path = "pickaxe/pickaxe" if tool == "pickaxe" else "tools/" + tool
    base_textures = {}
    broken = {}
    for layer, source in original["textures"].items():
        target = "item/antique/" + source.split(":items/", 1)[1]
        if layer.startswith("layer"):
            base_textures[layer] = texture(source, target)
        elif layer.startswith("broken"):
            broken["layer" + layer.removeprefix("broken")] = texture(source, target)
    base = {"parent": "minecraft:item/handheld", "textures": base_textures}
    if "display" in original:
        base["display"] = original["display"]
    overrides = []
    original_overrides = original.get("overrides", [])
    if isinstance(original_overrides, dict):
        original_overrides = [original_overrides]
    for index, override in enumerate(original_overrides):
        textures = dict(base_textures)
        for layer, source in override.get("textures", {}).items():
            textures[layer] = texture(source, "item/antique/" + source.split(":items/", 1)[1])
        variant = {"parent": "tconstruct:item/" + tool_path, "textures": textures}
        if "display" in override:
            variant["display"] = override["display"]
        variant_name = tool_path + f"_state_{index}"
        write(dest / f"models/item/{variant_name}.json", variant)
        predicate = {("charged" if key == "loaded" else key): value for key, value in override["predicate"].items()}
        overrides.append({"predicate": predicate, "model": "tconstruct:item/" + variant_name})
    if broken:
        variant_name = tool_path + "_broken"
        write(dest / f"models/item/{variant_name}.json", {"parent": "tconstruct:item/" + tool_path,
                                                        "textures": dict(base_textures, **broken)})
        overrides.append({"predicate": {"tconstruct:broken": 1}, "model": "tconstruct:item/" + variant_name})
    if overrides:
        base["overrides"] = overrides
    write(dest / f"models/item/{tool_path}.json", base)

# 铸模继续复用当前模组的 CastSpriteSource。
not_cast = {"arrow_shaft", "fletching", "bow_string"}
atlas_path = resources / "assets/minecraft/atlases/blocks.json"
atlas = json.loads(atlas_path.read_text())
atlas["sources"] = [row for row in atlas["sources"] if not row.get("sprite", "").startswith("tconstruct:item/cast_")]
for part, info in parts.items():
    if part in not_cast:
        continue
    mask = texture(info["source"], "item/cast_masks/" + part)
    atlas["sources"].append({"type": "tconstruct:cast", "sprite": "tconstruct:item/cast_" + part, "mask": mask,
                             "offset_x": info["offset"].get("x", 0), "offset_y": info["offset"].get("y", 0)})
    write(dest / f"models/item/cast_{part}.json", {"parent": "minecraft:item/generated", "textures": {"layer0": "tconstruct:item/cast_" + part}})
    write(dest / f"models/item/clay_cast_{part}.json", {"parent": "tconstruct:item/cast_" + part})
write(atlas_path, atlas)

supplies = {"reinforcement": "materials/reinforcement", "necrotic_bone": "materials/necrotic_bone",
            "silky_jewel": "materials/silky_jewel", "lava_crystal": "materials/lava_crystal",
            "mending_moss": "materials/mending_moss", "expander_horizontal": "materials/expander_w",
            "expander_vertical": "materials/expander_h", "bloodbone": "materials/bloody_bone",
            "bacon": "food/bacon", "pattern": "pattern", "silky_cloth": "materials/silky_cloth", "moss": "materials/moss"}
for item, source in supplies.items():
    tex = texture("tconstruct:items/" + source, "item/toolsupplies/" + item, neutral=False)
    write(dest / f"models/item/{item}.json", {"parent": "minecraft:item/generated", "textures": {"layer0": tex}})
write(dest / "models/item/creative_modifier.json", {"parent": "minecraft:item/generated", "textures": {"layer0": "tconstruct:item/toolsupplies/creative_modifier"}})
manifest.append({"source": None, "output": "textures/item/toolsupplies/creative_modifier.png", "neutral": False,
                 "description": "紫色八角创造水晶，金色中心符文，透明背景，16×16 像素"})
for effect in ["momentum", "insatiable", "splinter", "enderference", "bleed", "magnetic"]:
    manifest.append({"source": None, "output": f"textures/mob_effect/{effect}.png", "neutral": False,
                     "kind": effect, "description": "按效果名称绘制的现代像素状态图标，16×16 透明背景"})
forge = json.loads((assets / "models/block/toolforge.json").read_text())
forge["textures"] = {key: value.replace("minecraft:blocks/", "minecraft:block/").replace("tconstruct:blocks/", "tconstruct:block/")
                      for key, value in forge["textures"].items()}
texture("tconstruct:blocks/toolforge_top", "block/toolforge_top", neutral=False)
write(dest / "models/block/toolforge.json", forge)
write(dest / "blockstates/toolforge.json", {"variants": {"": {"model": "tconstruct:block/toolforge"}}})
write(dest / "models/item/toolforge.json", {"parent": "tconstruct:block/toolforge"})
for block, item in [("toolstation", "blockitem/toolstation"), ("toolforge", "toolforge")]:
    write(resources / f"data/tconstruct/loot_table/blocks/{block}.json", {"type": "minecraft:block", "pools": [{"rolls": 1,
          "entries": [{"type": "minecraft:item", "name": "tconstruct:" + item}], "conditions": [{"condition": "minecraft:survives_explosion"}]}]})
pickaxe_tag = resources / "data/minecraft/tags/block/mineable/pickaxe.json"
tag = json.loads(pickaxe_tag.read_text())
if "tconstruct:toolforge" not in tag["values"]:
    tag["values"].append("tconstruct:toolforge")
write(pickaxe_tag, tag)
write(args.output / "scripts/antique_tool_textures.json", manifest)


def ingredient(item):
    return {"tag": item[1:]} if item.startswith("#") else {"item": item}


def shaped(id, pattern, key, count=1):
    write(resources / f"data/tconstruct/recipe/tools/{id}.json", {
        "type": "minecraft:crafting_shaped", "pattern": pattern,
        "key": {symbol: ingredient(item) for symbol, item in key.items()},
        "result": {"id": "tconstruct:" + id, "count": count}})


shaped("pattern", ["SP", "PS"], {"S": "minecraft:stick", "P": "#minecraft:planks"}, 4)
write(resources / "data/tconstruct/recipe/tools/toolstation.json", {"type": "minecraft:crafting_shaped", "pattern": ["P", "C"],
      "key": {"P": {"item": "tconstruct:pattern"}, "C": {"item": "minecraft:crafting_table"}}, "result": {"id": "tconstruct:blockitem/toolstation"}})
shaped("toolforge", ["BBB", "MTM", "M M"], {"B": "tconstruct:seared_bricks", "M": "minecraft:iron_block", "T": "tconstruct:blockitem/toolstation"})
shaped("reinforcement", ["OOO", "OCO", "OOO"], {"O": "minecraft:obsidian", "C": "tconstruct:cast_blank"})
shaped("silky_cloth", ["SSS", "SGS", "SSS"], {"S": "minecraft:string", "G": "minecraft:gold_ingot"})
shaped("silky_jewel", [" C ", "CEC", " C "], {"C": "tconstruct:silky_cloth", "E": "minecraft:emerald"})
shaped("lava_crystal", ["BFB", "FWF", "BFB"], {"B": "minecraft:blaze_rod", "F": "minecraft:fire_charge", "W": "tconstruct:lavawood"})
shaped("moss", ["MMM", "MMM", "MMM"], {"M": "#tconstruct:mossy_stones"})
write(resources / "data/tconstruct/tags/item/mossy_stones.json", {"replace": False, "values": ["minecraft:mossy_cobblestone", "minecraft:mossy_stone_bricks"]})
shaped("expander_horizontal", [" L ", "PSP", " L "], {"L": "minecraft:lapis_lazuli", "P": "minecraft:piston", "S": "tconstruct:slimeball_purple"})
shaped("expander_vertical", [" P ", "LSL", " P "], {"L": "minecraft:lapis_lazuli", "P": "minecraft:piston", "S": "tconstruct:slimeball_purple"})


def lang_file(path):
    result = {}
    for line in path.read_text(encoding="utf-8-sig").splitlines():
        if "=" in line and not line.lstrip().startswith("#"):
            key, value = line.split("=", 1)
            result[key] = value.replace("\\n", "\n")
    return result


modifier_ids = "glowing haste sharpness diamond emerald luck fiery knockback bane_of_arthropods smite shulking webbed soulbound endearment incognito fins reinforced necrotic silktouch autosmelt mending_moss harvestwidth harvestheight creative beheading blasting".split()
trait_ids = "alien aquadynamic aridiculous autosmelt baconlicious coldblooded crude crumbling dense depthdigger duritos enderference established flammable fractured heavy hellish holy insatiable jagged lightweight magnetic momentum petramor poisonous prickly raging sharp shocking slimey_green slimey_blue spiky splintering splinters squeaky superheat stiff stonebound tasty unnatural writable breakable endspeed freezing hovering splitting".split()
for locale in ["zh_cn", "en_us"]:
    source = lang_file(assets / f"lang/{locale}.lang")
    path = dest / f"lang/{locale}.json"
    translated = json.loads(path.read_text(encoding="utf-8-sig"))
    for id in tool_ids + list(parts) + ["bolt_core"]:
        text = source.get(f"item.tconstruct.{id}.name", {"pickaxe_head": "镐头" if locale == "zh_cn" else "Pickaxe Head"}.get(id, id.replace("_", " ").title()))
        translated[f"item.tconstruct.{id}.name"] = text
        translated[f"item.tconstruct.tools.{id}"] = text
        translated[f"item.tconstruct.parts.{id}"] = text
        translated[f"container.tconstruct.toolstation.mode.{id}"] = text
    for part in parts:
        name = translated[f"item.tconstruct.{part}.name"]
        translated[f"item.tconstruct.cast_{part}"] = (name + "铸模") if locale == "zh_cn" else name + " Cast"
        translated[f"item.tconstruct.clay_cast_{part}"] = (name + "黏土铸模") if locale == "zh_cn" else name + " Clay Cast"
    for id in trait_ids:
        original = "slimey" if id.startswith("slimey_") else id
        translated[f"traits.{id}.name"] = source.get(f"modifier.{original}.name", original.title())
        translated[f"traits.{id}.description"] = source.get(f"modifier.{original}.desc", "")
    for id in modifier_ids:
        original = "bane_of_arthopods" if id == "bane_of_arthropods" else id
        translated[f"modifier.tconstruct.{id}"] = source.get(f"modifier.{original}.name", id.replace("_", " ").title())
    for id in supplies:
        translated[f"item.tconstruct.{id}"] = source.get(f"item.tconstruct.materials.{id}.name", source.get(f"item.tconstruct.{id}.name", id.replace("_", " ").title()))
    local = {
        "item.tconstruct.material_part": ["%s%s", "%s %s"],
        "message.tconstruct.pattern_selected": ["已选择：%s", "Selected: %s"],
        "tooltip.tconstruct.pattern_part": ["部件模板：%s", "Part pattern: %s"],
        "item.tconstruct.pattern.blank": ["空白模具", "Blank Pattern"],
        "tooltip.tconstruct.pattern_usage": ["右键切换部件，潜行右键反向切换；放入工具装配台的修复模式并加入材料。", "Right-click to select a part; sneak to reverse. Use with materials in the Tool Station's repair mode."],
        "tooltip.tconstruct.part_material": ["%s：%s", "%s: %s"],
        "tooltip.tconstruct.modifier": ["%s %s（%s）", "%s %s (%s)"],
        "item.tconstruct.bloodbone": ["血骨", "Bloodbone"],
        "item.tconstruct.expander_horizontal": ["横向扩展器", "Horizontal Expander"],
        "item.tconstruct.expander_vertical": ["纵向扩展器", "Vertical Expander"],
        "item.tconstruct.creative_modifier": ["创造强化水晶", "Creative Modifier"],
        "effect.tconstruct.momentum": ["动量", "Momentum"],
        "effect.tconstruct.insatiable": ["贪婪", "Insatiable"],
        "effect.tconstruct.splinter": ["碎片", "Splinter"],
        "effect.tconstruct.enderference": ["末影干扰", "Enderference"],
        "effect.tconstruct.bleed": ["流血", "Bleeding"],
        "effect.tconstruct.magnetic": ["磁力", "Magnetic"],
        "modifier.tconstruct.mending_moss": ["生态修复", "Mending Moss"],
        "modifier.tconstruct.endearment": ["额外强化", "Extra Modifier"],
        "modifier.tconstruct.creative": ["创造强化", "Creative Modifier"],
        "modifier.tconstruct.glowing": ["发光", "Glowing"],
        "tooltip.tconstruct.part_durability": ["基础耐久：%s", "Base durability: %s"],
        "tooltip.tconstruct.bow_stats": ["拉弓速度：%s；射程：%s；附加伤害：%s", "Draw speed: %s; range: %s; bonus damage: %s"],
        "tooltip.tconstruct.shaft_stats": ["弹药系数：%s；额外弹药：%s", "Ammo multiplier: %s; bonus ammo: %s"],
        "tooltip.tconstruct.fletching_stats": ["精度：%s；弹药系数：%s", "Accuracy: %s; ammo multiplier: %s"],
        "message.tconstruct.moss_levels": ["需要至少5级经验。", "At least 5 experience levels are required."],
        "tooltip.tconstruct.coating": ["包覆材料：%s", "Coating: %s"],
        "block.tconstruct.toolforge": ["工具锻造台", "Tool Forge"],
        "item.tconstruct.toolforge": ["工具锻造台", "Tool Forge"],
    }
    for key, values in local.items():
        translated[key] = values[0 if locale == "zh_cn" else 1]
    for id, name in [("prickly", "尖刺"), ("splinters", "碎刺"), ("bleed", "流血")]:
        translated[f"death.attack.tconstruct.{id}"] = f"%s因{name}倒下了" if locale == "zh_cn" else "%s bled out"
        translated[f"death.attack.tconstruct.{id}.player"] = f"%s在与%s交战后因{name}倒下了" if locale == "zh_cn" else "%s bled out while fighting %s"
    write(path, translated)
sounds = json.loads((dest / "sounds.json").read_text())
for id in ["frypan_hit", "toy_squeak", "charged", "discharge", "crossbow_reload"]:
    shutil.copyfile(assets / f"sounds/{id}.ogg", dest / f"sounds/{id}.ogg")
    sounds[id] = {"sounds": ["tconstruct:" + id]}
write(dest / "sounds.json", sounds)
for id in ["prickly", "splinters", "bleed"]:
    write(resources / f"data/tconstruct/damage_type/{id}.json", {"message_id": "tconstruct." + id, "scaling": "never", "exhaustion": 0.1})
write(resources / "data/minecraft/tags/damage_type/bypasses_armor.json", {"replace": False, "values": ["tconstruct:prickly", "tconstruct:splinters"]})
print(f"Prepared {len(manifest)} GIMP textures; {len(tool_ids)} tools and {len(parts) + 1} parts")
