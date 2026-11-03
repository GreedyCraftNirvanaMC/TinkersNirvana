"""在 GIMP MCP 的 PyGObject 上下文运行，保留 Antique 形状并重绘像素色阶。"""
import colorsys
import hashlib
import json
from pathlib import Path
from gi.repository import Gimp, Gegl, Gio


def export_png(image, path):
    path.parent.mkdir(parents=True, exist_ok=True)
    procedure = Gimp.get_pdb().lookup_procedure("file-png-export")
    config = procedure.create_config()
    config.set_property("run-mode", Gimp.RunMode.NONINTERACTIVE)
    config.set_property("image", image)
    config.set_property("file", Gio.File.new_for_path(str(path)))
    config.set_property("compression", 9)
    for flag in ["include-color-profile", "include-exif", "include-iptc", "include-xmp", "include-thumbnail", "include-comment", "bkgd", "offs", "phys", "time"]:
        config.set_property(flag, False)
    result = procedure.run(config)
    if result.index(0) != Gimp.PDBStatusType.SUCCESS:
        raise RuntimeError(str(result))


def restyle_tools(antique_root, output_root, start=0, end=None, manifest_name="antique_tool_textures.json"):
    root = Path(output_root)
    source_root = Path(antique_root) / "src/main/resources/assets/tconstruct"
    output = root / "src/main/resources/assets/tconstruct"
    rows = json.loads((root / "scripts" / manifest_name).read_text(encoding="utf-8"))
    for row in rows[start:end]:
        if row["source"] is None:
            image = Gimp.Image.new(16, 16, Gimp.ImageBaseType.RGB)
            layer = Gimp.Layer.new(image, "创造水晶", 16, 16, Gimp.ImageType.RGBA_IMAGE, 100, Gimp.LayerMode.NORMAL)
            image.insert_layer(layer, None, 0)
            pixels = bytearray(16 * 16 * 4)
            kind = row.get("kind", "creative")
            palette = [(52, 28, 77), (92, 48, 132), (157, 80, 205), (209, 143, 235), (246, 220, 255)]
            for y in range(2, 14):
                for x in range(2, 14):
                    distance = abs(x - 7.5) + abs(y - 7.5)
                    if distance > 8:
                        continue
                    shade = 0 if distance > 6 else 1 if x + y > 17 else 3 if x + y < 12 else 2
                    rgb = palette[shade]
                    if (x == 7 and 5 <= y <= 10) or (y == 7 and 5 <= x <= 10):
                        rgb = (255, 223, 113)
                    pixels[(y * 16 + x) * 4:(y * 16 + x) * 4 + 4] = bytes((*rgb, 255))
            if kind != "creative":
                pixels = bytearray(16 * 16 * 4)
                for y in range(2, 14):
                    for x in range(2, 14):
                        paint = False
                        rgb = (120, 210, 235)
                        if kind == "momentum":
                            paint = abs(x - (11 - abs(y - 7))) <= 1 or abs(x - (6 - abs(y - 7))) <= 1
                        elif kind == "insatiable":
                            paint = (y in (4, 11) and 3 <= x <= 12) or (x in (3, 12) and 4 <= y <= 11) or (x % 3 == 0 and y in (5, 6, 9, 10))
                            rgb = (193, 109, 225)
                        elif kind == "splinter":
                            paint = abs(x - y + 3) <= 1 or (abs(x - y - 3) <= 1 and 4 <= y <= 11)
                            rgb = (218, 198, 142)
                        elif kind == "enderference":
                            paint = (2 <= (x - 7.5) ** 2 + (y - 7.5) ** 2 <= 29) or abs(x + y - 15) <= 1
                            rgb = (55, 183, 155) if abs(x + y - 15) > 1 else (235, 194, 107)
                        elif kind == "bleed":
                            paint = (4 <= y <= 11 and abs(x - 7.5) <= (y - 3) / 2) or (y == 12 and 6 <= x <= 9)
                            rgb = (210, 49, 60)
                        elif kind == "magnetic":
                            paint = (x in (3, 4, 11, 12) and 3 <= y <= 10) or (y in (10, 11, 12) and 4 <= x <= 11)
                            rgb = (215, 71, 70) if y >= 6 else (218, 228, 231)
                        if paint:
                            shade = .72 if x + y > 17 else 1.12 if x + y < 11 else 1
                            rgba = bytes((*[min(255, round(channel * shade)) for channel in rgb], 255))
                            pixels[(y * 16 + x) * 4:(y * 16 + x) * 4 + 4] = rgba
            layer.get_buffer().set(Gegl.Rectangle.new(0, 0, 16, 16), "R'G'B'A u8", bytes(pixels))
        else:
            texture_root = source_root if row.get("source_mod", "tconstruct") == "tconstruct" else Path(antique_root).parent / "Mantle/src/main/resources/assets/mantle"
            source = texture_root / row["source"]
            if hashlib.sha256(source.read_bytes()).hexdigest() != row["source_sha256"]:
                raise ValueError("Source texture changed: " + str(source))
            image = Gimp.file_load(Gimp.RunMode.NONINTERACTIVE, Gio.File.new_for_path(str(source)))
            if "crop" in row:
                x, y, width, height = row["crop"]
                image.crop(width, height, x, y)
            if image.get_base_type() != Gimp.ImageBaseType.RGB:
                image.convert_rgb()
            layer = image.get_layers()[0]
            layer.add_alpha()
            width, height = image.get_width(), image.get_height()
            rect = Gegl.Rectangle.new(0, 0, width, height)
            original = bytes(layer.get_buffer().get(rect, 1.0, "R'G'B'A u8", Gegl.AbyssPolicy.NONE))
            pixels = bytearray(len(original))
            dx, dy = row.get("offset", [0, 0])
            for y in range(height):
                for x in range(width):
                    index = (y * width + x) * 4
                    red, green, blue, alpha = original[index:index + 4]
                    target_x, target_y = x + dx, y + dy
                    if not alpha or not (0 <= target_x < width and 0 <= target_y < height):
                        continue
                    if row.get("preserve_pixels"):
                        pass
                    elif row["neutral"]:
                        luminance = (red * .2126 + green * .7152 + blue * .0722) / 255
                        # 原版铁工具的窄高光、实色中间调与暗边；不新增抗锯齿或噪点。
                        ramp = [24, 68, 107, 150, 190, 216, 255]
                        shade = min(6, max(0, round((luminance ** .9) * 6)))
                        red = green = blue = ramp[shade]
                    else:
                        hue, lightness, saturation = colorsys.rgb_to_hls(red / 255, green / 255, blue / 255)
                        lightness = max(.08, min(.97, round((lightness ** .95) * 10) / 10))
                        red, green, blue = [round(value * 255) for value in colorsys.hls_to_rgb(hue, lightness, min(1, saturation * 1.05))]
                    target = (target_y * width + target_x) * 4
                    pixels[target:target + 4] = bytes((red, green, blue, alpha))
            layer.get_buffer().set(rect, "R'G'B'A u8", bytes(pixels))
            layer.update(0, 0, width, height)
        export_png(image, output / row["output"])
        image.delete()
    Gimp.displays_flush()
    print("GIMP exported", len(rows[start:end]), "textures")


def preview_tools(antique_root, output_root, start=0, end=21):
    global _tool_preview_sheet, _tool_preview_layer, _tool_preview_pixels
    root = Path(output_root)
    old = Path(antique_root) / "src/main/resources/assets/tconstruct"
    new = root / "src/main/resources/assets/tconstruct"
    ids = "pickaxe shovel hatchet mattock kama hammer excavator lumberaxe scythe broadsword longsword rapier frypan battlesign cleaver shortbow longbow crossbow arrow bolt shuriken".split()
    width, height = 7 * 20, 6 * 20 + 4
    if start == 0:
        _tool_preview_sheet = Gimp.Image.new(width, height, Gimp.ImageBaseType.RGB)
        _tool_preview_layer = Gimp.Layer.new(_tool_preview_sheet, "Antique 原图 / GIMP 新版", width, height, Gimp.ImageType.RGBA_IMAGE, 100, Gimp.LayerMode.NORMAL)
        _tool_preview_sheet.insert_layer(_tool_preview_layer, None, 0)
        _tool_preview_pixels = bytearray(bytes((39, 43, 48, 255)) * width * height)
    sheet, layer, pixels = _tool_preview_sheet, _tool_preview_layer, _tool_preview_pixels
    for version in (0, 1):
        for index, tool in enumerate(ids):
            if index < start or index >= end:
                continue
            path = "pickaxe/pickaxe" if tool == "pickaxe" else "tools/" + tool
            model = json.loads(((old / f"models/item/tools/{tool}.tcon.json") if version == 0 else (new / f"models/item/{path}.json")).read_text())
            for key, source in model["textures"].items():
                if not key.startswith("layer"):
                    continue
                file = (old if version == 0 else new) / ("textures/" + source.split(":", 1)[1] + ".png")
                image = Gimp.file_load(Gimp.RunMode.NONINTERACTIVE, Gio.File.new_for_path(str(file)))
                data = bytes(image.get_layers()[0].get_buffer().get(Gegl.Rectangle.new(0, 0, 16, 16), 1, "R'G'B'A u8", Gegl.AbyssPolicy.NONE))
                image.delete()
                tint = [(153, 108, 62), (184, 196, 204), (196, 141, 63), (151, 95, 44)][int(key[-1]) % 4]
                if "bow" in tool:
                    tint = (214, 214, 208) if "bowstring" in source else (167, 117, 66)
                elif tool in ("arrow", "bolt"):
                    tint = [(155, 115, 73), (196, 204, 212), (233, 224, 201)][int(key[-1]) % 3]
                for y in range(16):
                    for x in range(16):
                        rgba = data[(y * 16 + x) * 4:(y * 16 + x) * 4 + 4]
                        if len(rgba) != 4 or rgba[3] == 0:
                            continue
                        tx = index % 7 * 20 + x + 2
                        ty = index // 7 * 20 + y + 2 + version * 64
                        target = (ty * width + tx) * 4
                        alpha = rgba[3] / 255
                        rgb = [round(rgba[channel] * tint[channel] / 255 * alpha + pixels[target + channel] * (1 - alpha)) for channel in range(3)]
                        pixels[target:target + 4] = bytes((*rgb, 255))
    if end < len(ids):
        print("GIMP preview prepared", end)
        return
    layer.get_buffer().set(Gegl.Rectangle.new(0, 0, width, height), "R'G'B'A u8", bytes(pixels))
    Gimp.context_set_interpolation(Gimp.InterpolationType.NONE)
    sheet.scale(width * 6, height * 6)
    export_png(sheet, root / "docs/assets/antique-tools-comparison.png")
    sheet.delete()
    Gimp.displays_flush()
    print("GIMP comparison exported")
