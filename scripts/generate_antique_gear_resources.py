"""Native equipment models/recipes and a GIMP manifest; source equipment retains its silhouettes."""
import argparse
import hashlib
import json
import shutil
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


def texture(source, output, neutral=False, crop=None):
    manifest.append({"source": "textures/" + source + ".png", "output": "textures/" + output + ".png", "source_sha256": hashlib.sha256((old / ("textures/" + source + ".png")).read_bytes()).hexdigest(),
                     "neutral": neutral, **({"crop": crop} if crop else {})})
    return "tconstruct:" + output


for source, output, neutral in [("items/gadgets/slimesling", "item/gadgets/slimesling", True), ("items/armor/slime_boots", "item/gadgets/slime_boots", True), ("items/gadgets/piggybackpack", "item/gadgets/piggybackpack", False),
    ("models/armor/slime_layer_1", "models/armor/slime_layer_1", True), ("models/armor/piggyback_layer_1", "models/armor/piggyback_layer_1", False)]:
    texture(source, output, neutral)
for count in [1, 2, 3]:
    texture("gui/icons", "mob_effect/carry" + ("" if count == 1 else "_" + str(count)), crop=[234, (count - 1) * 18, 18, 18])

colors = {"green": ["绿色", "Green"], "blue": ["蓝色", "Blue"], "purple": ["紫色", "Purple"], "blood": ["血液", "Blood"], "magma": ["岩浆", "Magma"], "pink": ["粉色", "Pink"]}
for color, (zh, en) in colors.items():
    for type_, title, english, source in [("slimesling", "史莱姆弹弓", "Slimesling", "slimesling"), ("slime_boots", "史莱姆靴", "Slime Boots", "slime_boots")]:
        name = type_ + "_" + color
        model = json.loads((old / f"models/item/{source}.json").read_text())
        model["parent"] = "minecraft:" + model["parent"]
        model["textures"] = {"layer0": "tconstruct:item/gadgets/" + source}
        write(assets / f"models/item/{name}.json", model)
        names["item.tconstruct." + name] = [zh + title, en + " " + english]
    if color == "pink":
        continue
    ball, block = {"tag": "c:slime_balls/" + color}, {"item": "tconstruct:slime_congealed_" + color}
    for name, pattern, key in [("slimesling", ["SCS", "B B", " B "], {"S": {"item": "minecraft:string"}, "C": block, "B": ball}), ("slime_boots", ["A A", "B B"], {"A": ball, "B": block})]:
        write(root / f"data/tconstruct/recipe/gadgets/gear/{name}_{color}.json", {"type": "minecraft:crafting_shaped", "group": "tconstruct:" + name, "pattern": pattern, "key": key, "result": {"id": "tconstruct:" + name + "_" + color}})
for name in ["slimesling", "slime_boots"]:
    write(root / f"data/tconstruct/recipe/gadgets/gear/{name}_mixed.json", {"type": "tconstruct:" + name + "_mixing", "category": "misc"})
write(root / "data/tconstruct/tags/item/congealed_slime.json", {"replace": False, "values": ["tconstruct:slime_congealed_" + color for color in colors]})
write(assets / "models/item/piggybackpack.json", {"parent": "minecraft:item/generated", "textures": {"layer0": "tconstruct:item/gadgets/piggybackpack"}})
write(root / "data/tconstruct/recipe/gadgets/gear/piggybackpack.json", {"type": "minecraft:crafting_shaped", "pattern": [" A ", "B B", " A "], "key": {"A": {"tag": "c:rods/wooden"}, "B": {"tag": "c:leathers"}}, "result": {"id": "tconstruct:piggybackpack"}})
names.update({
    "item.tconstruct.piggybackpack": ["背架", "Piggybackpack"],
    "effect.tconstruct.carry": ["背负", "Carrying"],
    "tooltip.tconstruct.slimesling": ["站在地上对准方块蓄力，松手后向反方向弹射。", "Charge while standing on the ground and aiming at a block, then release to fling yourself backwards."],
    "tooltip.tconstruct.slime_boots": ["落地时弹跳并免疫摔落伤害；潜行停止弹跳并减轻摔落伤害。", "Bounce on landing without fall damage. Sneak to stop bouncing and reduce fall damage."],
    "tooltip.tconstruct.piggybackpack": ["手持背架右击生物即可背负；卸下背架会放下乘客。", "Right-click a creature to carry it. Remove the pack to release passengers."],
    "message.tconstruct.piggyback.cannot_pick_up": ["无法背负这个生物。", "You cannot carry this creature."],
    "message.tconstruct.piggyback.full": ["背负空间不足。", "There is no more room to carry passengers."],
    "message.tconstruct.piggyback.inventory_full": ["背包空间或背架数量不足。", "There is not enough inventory space or enough packs."],
})
for lang, column in [("zh_cn", 0), ("en_us", 1)]:
    target = assets / f"lang/{lang}.json"
    data = json.loads(target.read_text(encoding="utf-8-sig"))
    data.update({key: values[column] for key, values in names.items()})
    write(target, data)
shutil.copyfile(old / "sounds/slimesling.ogg", assets / "sounds/slimesling.ogg")
sounds = json.loads((assets / "sounds.json").read_text())
sounds["slimesling"] = {"sounds": ["tconstruct:slimesling"]}
write(assets / "sounds.json", sounds)
write(project / "scripts/antique_gear_textures.json", manifest)
print(f"Prepared colored gear and {len(manifest)} GIMP textures")
