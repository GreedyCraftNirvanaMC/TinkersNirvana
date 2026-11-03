# 匠魂宝典

默认首次进入世界时获得一本《匠魂宝典》。可用书和空白模板合成；三张纸、一根线和两个模板也可制作原版书。`general.spawnWithBook` 控制赠书，死亡不会重复赠书。

右击打开，左侧选择章节，顶部搜索标题或正文，底部按钮或左右方向键翻页。滚轮分别滚动目录与正文。材料属性、词条、工具、强化及原料来源读取当前注册内容，支持扩展材料。多方块可拖动旋转、点击层数按钮逐层查看，物品图标提供原生悬浮提示。

## 增加说明页

资源包可放置 `assets/<namespace>/book/pages/<language>/<section>/<name>.json`。语言采用 `en_us`、`zh_cn` 等；缺少当前语言时回退到英语。同一命名空间内相同 `id` 的翻译互相覆盖。

```json
{
  "id": "world/my_machine",
  "section": "world",
  "order": 20,
  "title": "我的机器",
  "text": [
    {"text": "将原料放入机器并提供能源。"},
    {"paragraph": true, "text": "再次使用可取出成品。"}
  ]
}
```

章节为 `intro`、`tools`、`materials`、`bowmaterials`、`traits`、`modifiers`、`seared_devices`、`world`。新增章节时，为 `book.tconstruct.section.<section>` 提供语言键。

`image` 支持 `file`、显示比例 `width` / `height`、原图尺寸 `texture_width` / `texture_height`。`tool` 或 `modifier` 可引用已注册标识符，自动加入部件或强化材料图标。

三维预览使用 `structure` 对象：

```json
{
  "size": [3, 2, 3],
  "structure": [
    {"pos": [0, 0, 0], "endPos": [2, 0, 2], "block": "tconstruct:seared_bricks"},
    {"pos": [1, 1, 0], "block": "tconstruct:smeltery_controller", "state": {"facing": "north"}}
  ]
}
```

结构各轴最多 32 格，坐标从零开始；后写入的方块覆盖同一位置。预览不会在世界中放置方块。
