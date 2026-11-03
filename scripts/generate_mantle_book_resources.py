"""Prepare Mantle-derived UI texture provenance and native book translations; export artwork in GIMP."""
import argparse
import hashlib
import json
from pathlib import Path

parser = argparse.ArgumentParser()
parser.add_argument("--mantle", type=Path, required=True)
args = parser.parse_args()
root = Path(__file__).resolve().parents[1]
assets = root / "src/main/resources/assets/tconstruct"
rows = []
for name in ("book", "bookfront"):
    source = f"textures/gui/book/{name}.png"
    file = args.mantle / "src/main/resources/assets/mantle" / source
    rows.append({"source_mod": "mantle", "source": source, "output": f"textures/gui/book/mantle_{name}.png", "source_sha256": hashlib.sha256(file.read_bytes()).hexdigest(), "neutral": False})
(root / "scripts/antique_book_ui_textures.json").write_text(json.dumps(rows, indent=2) + "\n", encoding="utf-8")
translations = {
    "book.tconstruct.cover_title": ["匠魂宝典", "Materials and You"],
    "book.tconstruct.cover_subtitle": ["作者：Skyla", "by Skyla"],
    "book.tconstruct.open": ["打开宝典", "Open book"],
    "book.tconstruct.cover": ["封面", "Cover"],
    "book.tconstruct.previous": ["上一页", "Previous spread"],
    "book.tconstruct.next": ["下一页", "Next spread"],
    "book.tconstruct.back": ["返回", "Back"],
    "book.tconstruct.search_button": ["搜索", "Search"],
    "book.tconstruct.search_results": ["搜索结果", "Search results"],
    "book.tconstruct.continued": ["%s（续）", "%s (continued)"],
    "book.tconstruct.structure.animate": ["切换逐层搭建动画", "Toggle layer animation"],
    "book.tconstruct.structure.reset": ["复位模型", "Reset model"],
    "book.tconstruct.structure.controls": ["拖动旋转；滚轮缩放", "Drag to rotate; scroll to zoom"],
    "book.tconstruct.material_details": ["材料与词条详情", "Material and trait details"],
}
for locale, column in (("zh_cn", 0), ("en_us", 1)):
    path = assets / f"lang/{locale}.json"
    data = json.loads(path.read_text(encoding="utf-8-sig"))
    data.update({key: value[column] for key, value in translations.items()})
    path.write_text(json.dumps(data, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
license_text = (args.mantle / "README.md").read_text(encoding="utf-8").split("## Licenses", 1)[1].split("Any alternate licenses", 1)[0].strip()
(root / "third_party/Mantle-LICENSE.txt").write_text(license_text + "\n", encoding="utf-8")
print("Prepared Mantle book UI provenance, license, and bilingual controls")
