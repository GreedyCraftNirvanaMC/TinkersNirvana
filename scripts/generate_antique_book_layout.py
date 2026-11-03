"""Import the active Antique book definition and original labels without changing artwork."""
import argparse
import json
import re
from pathlib import Path

parser = argparse.ArgumentParser()
parser.add_argument("--antique", type=Path, required=True)
args = parser.parse_args()
root = Path(__file__).resolve().parents[1]
assets = root / "src/main/resources/assets/tconstruct"
original = args.antique / "src/main/resources/assets/tconstruct"
modifier_colors = {}
for file in (args.antique / "src/main/java/slimeknights/tconstruct/tools/modifiers").glob("*.java"):
    for identifier, color in re.findall(r'super\("([a-z_]+)",\s*(0x[\da-fA-F]+)', file.read_text(encoding="utf-8")):
        modifier_colors[identifier] = int(color, 16)
modifier_colors.update({"harvestheight": 0xcaf6a2, "harvestwidth": 0xcaf6a2})

def write(path, value):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(value, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")

def language(path):
    result = {}
    if path.exists():
        for row in path.read_text(encoding="utf-8-sig").splitlines():
            if "=" in row and not row.startswith(("#", "//")):
                key, value = row.split("=", 1)
                result[key] = value.replace("\\n", "\n")
    return result

definition = []
for section in json.loads((original / "book/index.json").read_text()):
    name = section["name"]
    entries = json.loads((original / "book" / section["data"]).read_text()) if "data" in section else []
    ids = [name + "/" + (Path(row["data"]).stem if "data" in row else "blank_" + str(index)) for index, row in enumerate(entries)]
    icon = section.get("icon", {}).get("item", {}).get("id", "minecraft:book")
    if icon == "tconstruct:edible":
        icon = "tconstruct:slimeball_blue"  # original edible metadata 1
    definition.append({"id": name, "icon": icon, "pages": ids})
    for locale in sorted((original / "book").iterdir()):
        if not locale.is_dir() or locale.name == "sections":
            continue
        labels = language(locale / "language.lang")
        for index, entry in enumerate(entries):
            if "data" not in entry:
                data = {"title": "", "text": []}
            else:
                file = locale / entry["data"]
                if not file.exists():
                    continue
                raw = json.loads(re.sub(r",\s*([}\]])", r"\1", file.read_text(encoding="utf-8-sig")))
                existing = list((assets / f"book/pages/{locale.name}/{name}").glob(f"{index:02}_*.json"))
                data = json.loads(existing[0].read_text(encoding="utf-8")) if existing and name != "intro" else dict(raw)
                data["title"] = raw.get("title", "")
            data.update({"id": ids[index], "section": name, "order": index, "layout": entry["type"], "show_in_contents": entry.get("name") != "hidden", "titleless": not data.get("title")})
            data["listing_title"] = labels.get(entry.get("name", ""), data.get("title", ""))
            if "tool" in data or "modifier" in data:
                data["properties_label"] = labels.get("modifier.effect" if "modifier" in data else "tool.properties", "Properties")
            if "modifier" in data:
                data["title_color"] = modifier_colors.get(data["modifier"], 0xdddddd)
            file_name = Path(entry["data"]).stem if "data" in entry else "blank_" + str(index)
            write(assets / f"book/pages/{locale.name}/{name}/{index:02}_{file_name}.json", data)
write(assets / "book/definition.json", {"sections": definition})

base = language(original / "lang/en_us.lang")
keys = [key for key in base if key.startswith("stat.") or key.startswith("material.") and key.endswith(".name")]
for locale in ("en_us", "zh_cn"):
    source = dict(base)
    source.update(language(original / f"lang/{locale}.lang"))
    path = assets / f"lang/{locale}.json"
    current = json.loads(path.read_text(encoding="utf-8-sig"))
    book_labels = language(original / f"book/{locale}/language.lang")
    for section in definition:
        if section["id"] in book_labels:
            current["book.tconstruct.section." + section["id"]] = book_labels[section["id"]]
    appearance = json.loads((original / "book/appearance.json").read_text().replace("0xffce85", str(0xffce85)))
    current["book.tconstruct.cover_title"] = appearance["title"]
    current["book.tconstruct.cover_subtitle"] = appearance["subtitle"]
    for key in keys:
        value = source.get(key, base[key]); visited = {key}
        while value in source and value not in visited:
            visited.add(value); value = source[value]
        current[key] = value
    write(path, current)
print(f"Imported {len(definition)} original sections and {len(keys)} material/stat labels per language")
