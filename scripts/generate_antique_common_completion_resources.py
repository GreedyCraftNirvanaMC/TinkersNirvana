"""Remaining common supplies, EFLN, and sharpening-kit repair registration."""
import argparse
import hashlib
import json
from pathlib import Path

parser = argparse.ArgumentParser()
parser.add_argument("--antique", type=Path, required=True)
args = parser.parse_args()
project = Path(__file__).resolve().parents[1]
root = project / "src/main/resources"
assets = root / "assets/tconsnirvana"
old = args.antique / "src/main/resources/assets/tconstruct"
manifest, names = [], {}


def write(path, value):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(value, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")


for name, folder, zh, en in [("venomous_bone", "materials", "毒骨", "Venomous Bone"), ("blazing_bone", "materials", "炽血骨", "Blazing Bone"), ("efln", "gadgets", "EFLN 爆破弹", "EFLN")]:
    source = f"textures/items/{folder}/{name}.png"
    output = f"textures/item/gadgets/{name}.png"
    manifest.append({"source": source, "output": output, "source_sha256": hashlib.sha256((old / source).read_bytes()).hexdigest(), "neutral": False})
    write(assets / f"models/item/{name}.json", {"parent": "minecraft:item/generated", "textures": {"layer0": "tconsnirvana:item/gadgets/" + name}})
    names["item.tconsnirvana." + name] = [zh, en]
names["tooltip.tconsnirvana.efln"] = ["投掷后逐步炸开球形区域，并保留方块掉落物。", "Throws a gradual spherical blast that preserves block drops."]
names["death.attack.tconsnirvana.fiery"] = ["%s被炽热强化灼伤了", "%s was scorched"]
names["death.attack.tconsnirvana.fiery.player"] = ["%s在与%s交战时被炽热强化灼伤了", "%s was scorched while fighting %s"]
names["item.tconsnirvana.pattern.blank"] = ["空白模具", "Blank Pattern"]
for lang, column in [("zh_cn", 0), ("en_us", 1)]:
    target = assets / f"lang/{lang}.json"
    data = json.loads(target.read_text(encoding="utf-8-sig"))
    data.update({key: values[column] for key, values in names.items()})
    write(target, data)
write(root / "data/tconsnirvana/recipe/repair.json", {"type": "tconsnirvana:repair", "category": "equipment"})
write(root / "data/tconsnirvana/recipe/flint.json", {"neoforge:conditions": [{"type": "tconsnirvana:flint_recipe_enabled"}], "type": "minecraft:crafting_shapeless", "ingredients": [{"item": "minecraft:gravel"}] * 3, "result": {"id": "minecraft:flint"}})
write(root / "data/tconsnirvana/damage_type/fiery.json", {"message_id": "tconsnirvana.fiery", "scaling": "never", "exhaustion": .1})
for tag, values in [("is_fire", ["tconsnirvana:fiery"]), ("bypasses_armor", ["tconsnirvana:fiery"]), ("no_knockback", ["tconsnirvana:prickly", "tconsnirvana:splinters", "tconsnirvana:bleed", "tconsnirvana:fiery"])]:
    path = root / f"data/minecraft/tags/damage_type/{tag}.json"
    data = json.loads(path.read_text()) if path.exists() else {"replace": False, "values": []}
    data["values"] = list(dict.fromkeys(data["values"] + values))
    write(path, data)
write(root / "data/tconsnirvana/recipe/gadgets/efln.json", {"type": "minecraft:crafting_shapeless", "ingredients": [{"item": "minecraft:flint"}, [{"item": "minecraft:gunpowder"}, {"tag": "c:dusts/sulfur"}]], "result": {"id": "tconsnirvana:efln"}})
write(project / "scripts/antique_common_completion_textures.json", manifest)
print("Prepared three remaining items and native kit repair")
