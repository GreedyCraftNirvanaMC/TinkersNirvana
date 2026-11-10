# Materials and You

[README](../../README.md) · [简体中文](../zh-CN/BOOK.md)

By default, you receive a copy of the book the first time you enter a world. You can also craft it from a book and a blank pattern. Three paper, one string, and two patterns can make a vanilla book. `general.spawnWithBook` controls the initial gift. Dying does not grant another copy.

Right-click to open the cover, then click the cover or the bottom-right arrow to enter the icon index. The book follows Antique and Mantle's chapters, fixed pagination, and paper coordinates. Tool pages show assembly diagrams, material pages show one material each, ranged materials are grouped by stat with up to three per page, and modifier pages show their ingredients. Each copy stores its own reading position and works in either hand. Mantle is not required.

Use the bottom arrows, Left/Right, A/D, or the mouse wheel to turn pages. The top-left button opens the index. The return arrow under the book goes back to the page before the last link jump. Turning a page or opening the index clears that return point. The materials index uses item icons. Material sidebars show representative items, crafting methods, and finished tools. Hover over a stat or trait to read its description.

Stats, traits, tools, modifiers, and ingredients use the live registries, including add-on materials. Drag a multiblock to rotate it and use the loop arrow to toggle the block-by-block construction animation. Item and trait icons support native tooltips.

Default material stats come from Antique. The book reads current registry data when opened, while tutorial text and illustrations come from resource-pack JSON. Optional compatibility materials such as lead, silver, and electrum appear only when their ingredient tags are populated. See the [tool extension API](TOOL_API.md) for add-on visibility rules.

## Add a page

Put pages at `assets/<namespace>/book/pages/<language>/<section>/<name>.json`. Use language codes such as `en_us` and `zh_cn`. Missing translations fall back to English. Within a namespace, translations of the same `id` override one another.

```json
{
  "id": "world/my_machine",
  "section": "world",
  "order": 20,
  "title": "My Machine",
  "text": [
    {"text": "Supply ingredients and power to the machine."},
    {"paragraph": true, "text": "Use it again to retrieve the output."}
  ]
}
```

The built-in sections are `intro`, `tools`, `materials`, `bowmaterials`, `modifiers`, and `seared_devices`. For a new section, supply `book.tconstruct.section.<section>` in the language file. Built-in pages are declared in `assets/tconstruct/book/definition.json`. When replacing that namespace's section definition, list the page IDs to enable. Pages in other namespaces join their declared sections automatically.

The main index also links directly to the Seared Furnace and Tinker Tank introductions. Construction instructions and structure previews follow those pages. Resource packs can add index shortcuts through the `shortcuts` array in `definition.json`, for example `{"page": "tconstruct:seared_devices/tinker_tank_intro", "icon": "tconstruct:tinker_tank_controller"}`. A shortcut uses the target's `listing_title` or its page title. Missing targets are skipped and shortcuts do not duplicate page content.

Supported layouts are `blank`, `text`, `image with text below`, `imageText2`, `tool`, `modifier`, and `structure`. Images support `file`, display `width` and `height`, and source `texture_width` and `texture_height`. `imageText2` centers the image and follows it immediately with text. `image with text below` places the text under the image region. Keep fixed content within the 182×176 paper area. A registered `tool` or `modifier` ID supplies part or ingredient icons. Use `show_in_contents: false` to hide a section entry and `listing_title` for a separate listing name.

Fragments in the `text` array flow together. Set `paragraph: true` for a new paragraph or use `\n` for a line break. A fragment supports `color`, `bold`, `italic`, `underlined`, `strikethrough`, `obfuscated`, a string-array `tooltip`, and internal links through `action: "go-to-page:<namespace>:<page-id>"`. Antique's `go-to-page-rtn:` and `section.page` syntax also work. Links can only navigate within the book.

A 3D preview uses a `structure` object.

```json
{
  "size": [3, 2, 3],
  "structure": [
    {"pos": [0, 0, 0], "endPos": [2, 0, 2], "block": "tconstruct:seared_bricks"},
    {"pos": [1, 1, 0], "block": "tconstruct:smeltery_controller", "state": {"facing": "north"}}
  ]
}
```

Each axis can be at most 32 blocks, with zero-based coordinates. The first matching block definition wins at each position. Previews do not place blocks in the world.
