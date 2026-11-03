"""Keep Antique's complete guide text and diagrams; normalize legacy multiblock IDs."""
import argparse
import hashlib
import json
import struct
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


for file in [old / "textures/items/book.png", *sorted((old / "textures/gui/book").glob("*.png"))]:
    source = file.relative_to(old).as_posix()
    output = source.replace("textures/items/", "textures/item/")
    manifest.append({"source": source, "output": output, "source_sha256": hashlib.sha256(file.read_bytes()).hexdigest(), "neutral": False})

sections = ["intro", "tools", "modifiers", "seared_devices"]
for locale in sorted(path for path in (old / "book").iterdir() if path.is_dir() and path.name != "sections"):
    labels = {}
    language = locale / "language.lang"
    if language.exists():
        for line in language.read_text(encoding="utf-8-sig").splitlines():
            if "=" in line and not line.startswith(("#", "//")):
                key, value = line.split("=", 1)
                labels[key] = value
    write(assets / f"book/language/{locale.name}.json", labels)
    for section in sections:
        descriptors = json.loads((old / f"book/sections/{section}.json").read_text())
        for index, descriptor in enumerate(descriptors):
            path = locale / descriptor["data"]
            if not path.exists():
                continue
            try:
                data = json.loads(path.read_text(encoding="utf-8-sig"))
            except json.JSONDecodeError:
                # Some translations contain trailing commas, accepted by Mantle's lenient reader.
                import re
                data = json.loads(re.sub(r",\s*([}\]])", r"\1", path.read_text(encoding="utf-8-sig")))
            name = Path(descriptor["data"]).stem
            data.update({"section": section, "order": index, "id": section + "/" + name})
            if "data" in data:
                structure = json.loads((locale / data.pop("data")).read_text())
                for row in structure["structure"]:
                    identifier, meta = row["block"], row.pop("meta", 0)
                    row["block"] = {"tconstruct:seared": "tconstruct:seared_bricks", "tconstruct:seared_tank": {1: "tconstruct:seared_gauge", 2: "tconstruct:seared_window"}.get(meta, "tconstruct:seared_tank"), "tconstruct:smeltery_io": "tconstruct:smeltery_drain"}.get(identifier, identifier)
                data["structure"] = structure
            if not data.get("title"):
                data["title"] = labels.get(descriptor.get("name", ""), "")
                for device in ("smeltery", "seared_furnace", "tinker_tank"):
                    if name.startswith(device + "_") and not data["title"]:
                        title = labels.get(device + "_intro", device.replace("_", " ").title())
                        suffix = ("结构预览" if name.endswith("_multiblock") else "建造说明") if locale.name == "zh_cn" else ("Structure Preview" if name.endswith("_multiblock") else "Construction")
                        data["title"] = title + " · " + suffix
                if not data["title"] and "tool" not in data and "modifier" not in data:
                    previous_name = name.rstrip("0123456789")
                    data["title"] = labels.get(previous_name, "")
                    if not data["title"] and previous_name == "welcome":
                        data["title"] = {"zh_cn": "欢迎", "zh_tw": "歡迎"}.get(locale.name, "Welcome")
            if "image" in data:
                data["image"]["file"] = data["image"]["file"].replace(":textures/items/", ":textures/item/")
                image = data["image"]
                tw, th = struct.unpack(">II", (old / image["file"].split(":", 1)[1]).read_bytes()[16:24])
                image["texture_width"], image["texture_height"] = tw, th
                if "width" not in image:
                    image["width"], image["height"] = 150, round(150 * th / tw)
            write(assets / f"book/pages/{locale.name}/{section}/{index:02}_{name}.json", data)

names = {
    "modifier.tconstruct.fortify": ["磨制", "Fortified"],
    "modifier.tconstruct.embossment": ["镶饰", "Embossment"],
    "tooltip.tconstruct.fortified": ["磨制：%s（%s）", "Fortified with %s (%s)"],
    "item.tconstruct.book": ["匠魂宝典", "Materials and You"],
    "book.tconstruct.search": ["搜索章节、材料与词条", "Search chapters, materials, and traits"],
    "book.tconstruct.contents": ["目录", "Contents"],
    "book.tconstruct.section.intro": ["入门", "Getting Started"],
    "book.tconstruct.section.tools": ["工具", "Tools"],
    "book.tconstruct.section.materials": ["工具材料", "Tool Materials"],
    "book.tconstruct.section.bowmaterials": ["远程材料", "Ranged Materials"],
    "book.tconstruct.section.traits": ["材料词条", "Material Traits"],
    "book.tconstruct.section.modifiers": ["强化", "Modifiers"],
    "book.tconstruct.section.seared_devices": ["冶炼设备", "Seared Devices"],
    "book.tconstruct.section.world": ["世界与装置", "World and Gadgets"],
    "book.tconstruct.layer.all": ["全部层", "All layers"],
    "book.tconstruct.layer": ["第 %s 层", "Layer %s"],
    "book.tconstruct.structure_hint": ["拖动模型旋转，点击层数逐层查看。", "Drag the model to rotate; use the layer button to inspect each layer."],
    "book.tconstruct.craftable": ["可在部件加工台制作", "Craft parts in the part builder"],
    "book.tconstruct.castable": ["可在铸造台浇铸", "Cast parts on a casting table"],
    "book.tconstruct.modifier_limits": ["等级上限：%s；每级材料数：%s", "Maximum level: %s; ingredients per level: %s"],
    "book.tconstruct.parts": ["部件：", "Parts:"],
    "book.tconstruct.properties": ["属性：", "Properties:"],
    "book.tconstruct.ingredients": ["材料来源：", "Material sources:"],
    "book.tconstruct.material_value": ["%s：%s 份材料", "%s: %s material units"],
    "book.tconstruct.traits": ["词条：", "Traits:"],
    "book.tconstruct.empty_search": ["没有匹配的内容", "No matching pages"],
}
for field, zh, en in [("Head", "顶端", "Head"), ("Handle", "手柄", "Handle"), ("Extra", "附件", "Extra"), ("Bow", "弓臂", "Bow Limb"), ("BowString", "弓弦", "Bowstring"), ("Shaft", "箭杆", "Arrow Shaft"), ("Fletching", "箭羽", "Fletching")]:
    names["book.tconstruct.group." + field] = [zh, en]
for field, zh, en in [("durability", "耐久", "Durability"), ("speed", "采掘速度", "Mining speed"), ("attack", "攻击伤害", "Attack damage"), ("tier", "采掘等级", "Harvest tier"), ("modifier", "倍率", "Multiplier"), ("draw", "拉弓速度", "Draw speed"), ("range", "射程", "Range"), ("bonus", "额外伤害", "Bonus damage"), ("accuracy", "精准度", "Accuracy"), ("ammo", "额外弹药", "Bonus ammunition")]:
    names["book.tconstruct.stat." + field] = [zh + "：%s", en + ": %s"]

extra = [
    ("world/islands", "黏液岛与植物", "Slime Islands and Plants", "空岛可在主世界高空生成；熔岩岛可在下界熔岩海生成。岛上有黏液土、彩色草、树叶和藤蔓。黏液树苗需要种在黏液土上。树叶可用剪刀或精准采集获取，黏液草和蕨类可用骨粉增殖。地下黏液池默认关闭，可在配置中启用。", "Slime islands generate above the Overworld; magma islands may form on Nether lava seas. Find slime dirt, colored grass, leaves, and vines there. Slime saplings require slime soil. Shears or Silk Touch collect leaves; bone meal grows grass and ferns. Underground pools are disabled by default and can be enabled in the configuration."),
    ("world/gear", "弹弓、靴子与背架", "Slings, Boots, and Piggybackpacks", "黏液弹弓需要站在地上，对准方块按住使用键蓄力，松开后向后弹射。黏液靴可在落地时弹跳，潜行可停止弹跳并减少摔落伤害。手持背架右击生物即可背负，卸下背架会放下乘客。", "Charge a slimesling on the ground while aiming at a block, then release to launch backwards. Slime boots bounce on landing; sneak to stop bouncing and reduce fall damage. Right-click a creature with a piggybackpack to carry it; removing the pack releases its passengers."),
    ("world/drying", "晾干与储物", "Drying and Storage", "将肉、黏液球或黏土放在晾干架上，等待转化。晾干过程中只能手动取回原料，自动化只能取出成品。比较器在成品就绪时输出信号。物品架可展示物品。模板箱可储存模板或铸模；部件箱保存材料不同的部件。", "Put meat, slimeballs, or clay on a drying rack and wait for processing. Unfinished ingredients can be removed by hand; automation extracts finished products. Comparators signal completed output. Item racks display items. Pattern chests store patterns or casts; part chests store material-specific parts."),
    ("world/channels", "黏液通道", "Slime Channels", "烧炼凝固黏液可获得黏液通道。点击方块表面的不同位置可选择流向，潜行放置可反向。通道支持地面、墙壁、天花板和转角，可运输生物和掉落物。红石信号会停止流动，通道内的掉落物不会因时间而消失。", "Smelt congealed slime to make slime channels. Placement position selects flow direction; sneak to reverse it. Channels work on floors, walls, ceilings, and corners, carrying entities and dropped items. Redstone stops the flow. Items inside channels are protected from expiration."),
    ("world/gadgets", "装置与装饰", "Gadgets and Decorations", "木漏斗提供单槽物流。木轨道、活板门轨道、尖刺和褐砂石道路可用于建造运输和陷阱。不同材质的精美展示框支持原生物品旋转与地图；透明展示框在装入物品后隐藏边框。", "Wooden hoppers provide single-slot transport. Wooden rails, trapdoor rails, punji sticks, and brownstone roads support transport and traps. Fancy frames retain native item rotation and maps. A clear frame hides its border while displaying an item."),
]
for order, (identifier, zh, en, ztext, etext) in enumerate(extra):
    for locale, title, text in [("zh_cn", zh, ztext), ("en_us", en, etext)]:
        write(assets / f"book/pages/{locale}/{identifier}.json", {"id": identifier, "section": "world", "order": order, "title": title, "text": [{"text": text}]})
for lang, column in [("zh_cn", 0), ("en_us", 1)]:
    target = assets / f"lang/{lang}.json"
    data = json.loads(target.read_text(encoding="utf-8-sig"))
    data.update({key: values[column] for key, values in names.items()})
    write(target, data)
write(assets / "models/item/book.json", {"parent": "minecraft:item/generated", "textures": {"layer0": "tconstruct:item/book"}})
write(root / "data/tconstruct/recipe/book.json", {"type": "minecraft:crafting_shapeless", "ingredients": [{"item": "minecraft:book"}, {"item": "tconstruct:pattern"}], "result": {"id": "tconstruct:book"}})
write(root / "data/tconstruct/recipe/book_from_paper.json", {"type": "minecraft:crafting_shapeless", "ingredients": [{"item": "minecraft:paper"}] * 3 + [{"item": "minecraft:string"}] + [{"item": "tconstruct:pattern"}] * 2, "result": {"id": "minecraft:book"}})
write(project / "scripts/antique_book_textures.json", manifest)
print(f"Prepared complete translated book text and {len(manifest)} GIMP textures")
