"""Port Antique's channel geometry/rotations to native NeoForge block models."""
import argparse
import copy
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


def write(path, value):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(value, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")


manifest = []
for name in ["liquid_flow", "slime_channel_diagonal", "slime_channel_still", "slime_channel_still_diagonal"]:
    source = f"textures/blocks/fluids/{name}.png"
    output = f"textures/block/fluids/{name}.png"
    manifest.append({"source": source, "output": output, "source_sha256": hashlib.sha256((old / source).read_bytes()).hexdigest(), "neutral": True})
    meta = old / (source + ".mcmeta")
    if meta.exists():
        shutil.copyfile(meta, assets / (output + ".mcmeta"))

for kind in ["slime_channel", "slime_channel_inner", "slime_channel_outer", "slime_channel_diagonal"]:
    model = json.loads((old / f"models/block/{kind}.json").read_text())
    model["parent"] = "minecraft:block/block"
    model["render_type"] = "minecraft:translucent"
    model["ambientocclusion"] = False
    write(assets / f"models/block/slime_channel/{kind}.json", model)

variants = {}
for side in ["down", "up", "north", "south", "west", "east"]:
    source = json.loads((old / f"blockstates/slime_channel_{side}.json").read_text())
    for connection, base in source["variants"]["connected"].items():
        for direction, override in source["variants"]["direction"].items():
            for powered in ["false", "true"]:
                kind = override.get("model", base.get("model", source["defaults"]["model"])).split(":")[1]
                textures = copy.deepcopy(source["defaults"]["textures"])
                textures.update(source["variants"]["powered"][powered].get("textures", {}))
                textures = {key: value.replace(":blocks/", ":block/") for key, value in textures.items()}
                name = f"{side}_{connection}_{direction}_{powered}"
                model = {"parent": f"tconstruct:block/slime_channel/{kind}", "textures": textures}
                if "transform" in override:
                    model["transform"] = dict(override["transform"], origin="center")
                write(assets / f"models/block/slime_channel/{name}.json", model)
                variants[f"side={side},connected={connection},direction={direction},powered={powered}"] = dict({"model": f"tconstruct:block/slime_channel/{name}"}, **{key: override[key] for key in ["x", "y"] if key in override})

names = {}
for color, zh, en in [("green", "绿", "Green"), ("blue", "蓝", "Blue"), ("purple", "紫", "Purple"), ("blood", "血", "Blood"), ("magma", "岩浆", "Magma"), ("pink", "混色", "Mixed")]:
    name = f"slime_channel_{color}"
    write(assets / f"blockstates/{name}.json", {"variants": variants})
    write(assets / f"models/item/{name}.json", {"parent": "tconstruct:block/slime_channel/slime_channel", "textures": {"slime": "tconstruct:block/fluids/liquid_flow"}})
    write(root / f"data/tconstruct/recipe/gadgets/{name}.json", {"type": "minecraft:smelting", "ingredient": {"item": f"tconstruct:slime_congealed_{color}"}, "result": {"id": f"tconstruct:{name}", "count": 3}, "experience": .15, "cookingtime": 200})
    write(root / f"data/tconstruct/loot_table/blocks/{name}.json", {"type": "minecraft:block", "pools": [{"rolls": 1, "entries": [{"type": "minecraft:item", "name": f"tconstruct:{name}"}], "conditions": [{"condition": "minecraft:survives_explosion"}]}]})
    names[f"block.tconstruct.{name}"] = [zh + "黏液通道", en + " Slime Channel"]
names["tooltip.tconstruct.slime_channel"] = ["按落点选择流向，潜行反转；红石信号停止流动。", "Placement selects flow direction; sneak to reverse it. Redstone stops the flow."]
for lang, column in [("zh_cn", 0), ("en_us", 1)]:
    target = assets / f"lang/{lang}.json"
    data = json.loads(target.read_text(encoding="utf-8-sig"))
    data.update({key: values[column] for key, values in names.items()})
    write(target, data)
write(project / "scripts/antique_channel_textures.json", manifest)
print("Prepared six slime channels, 288 shared variants, and four GIMP textures")
