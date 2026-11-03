# 匠魂宝典

默认首次进入世界时获得一本《匠魂宝典》。可用书和空白模板合成；三张纸、一根线和两个模板也可制作原版书。`general.spawnWithBook` 控制赠书，死亡不会重复赠书。

右击打开封面，点击封面或右下角箭头进入图标目录。宝典沿用 Antique 与 Mantle 的章节、固定分页和纸面坐标：工具页展示部件装配图，材料每页一种，远程材料按属性分类、每页最多三种，强化页展示所需原料。每本书分别保存阅读位置，主手和副手均可使用。不需要安装 Mantle。

点击底部箭头，按左右方向键、A/D，或滚动鼠标滚轮翻页。左上角按钮返回目录，书下方的返回箭头返回上一次链接跳转前的位置；翻页或返回目录会清除该返回位置。材料目录使用物品图标；材料侧栏展示代表物品、制作方式和成品工具，悬停属性或词条可查看说明。

材料属性、词条、工具、强化及原料读取当前注册内容，支持扩展材料。多方块可拖动旋转，循环箭头切换逐块搭建动画。物品图标和词条支持原生悬浮提示。

默认材料属性来自 Antique 的材料定义，宝典使用打开时的注册表数据；教程文字与插图来自资源包 JSON。铅、银、琥珀金等兼容材料只有在对应原料标签非空时才出现在材料目录中。扩展材料的可见条件见 [工具扩展接口](TOOL_API.md)。

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

原版章节为 `intro`、`tools`、`materials`、`bowmaterials`、`modifiers`、`seared_devices`。新增章节时，为 `book.tconstruct.section.<section>` 提供语言键。模组自身的原版页面由 `assets/tconstruct/book/definition.json` 声明；替换该命名空间的章节定义时，同步列出要启用的页面 `id`。其他命名空间的页面自动加入对应章节。

`layout` 支持原版的 `blank`、`text`、`image with text below`、`imageText2`、`tool`、`modifier`、`structure`。`image` 支持 `file`、显示尺寸 `width` / `height`、原图尺寸 `texture_width` / `texture_height`；`imageText2` 居中放置图片后紧接正文，`image with text below` 将正文放在图片区域下方。固定页面的内容应控制在 182×176 纸面内。`tool` 或 `modifier` 可引用已注册标识符，自动加入部件或强化材料图标。`show_in_contents: false` 隐藏章节目录项，`listing_title` 单独指定目录名称。

`text` 数组中的片段连续排版，`paragraph: true` 开始新段落，`\n` 换行。片段可设置 `color`、`bold`、`italic`、`underlined`、`strikethrough`、`obfuscated`、字符串数组 `tooltip`，以及 `action: "go-to-page:<namespace>:<page-id>"` 书内链接。也兼容 Antique 的 `go-to-page-rtn:` 和 `section.page` 写法。所有链接都只跳转书内页面。

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

结构各轴最多 32 格，坐标从零开始；同一位置使用首个匹配的方块定义。预览不会在世界中放置方块。
