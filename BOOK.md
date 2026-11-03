# 匠魂宝典

默认首次进入世界时获得一本《匠魂宝典》。可用书和空白模板合成；三张纸、一根线和两个模板也可制作原版书。`general.spawnWithBook` 控制赠书，死亡不会重复赠书。

右击打开封面，点击封面或右下角箭头进入图标目录。宝典采用 Mantle 的双页布局：工具页展示部件装配图，材料页分栏显示属性和词条，强化页展示所需原料。长内容自动分页；每本书分别保存阅读位置，主手和副手均可使用。不需要安装 Mantle。

点击底部箭头，按左右方向键、A/D、Page Up/Page Down，或在正文上滚动鼠标滚轮翻页。左上角按钮和 Home 键返回目录，书下方的返回箭头沿跳转历史返回。右上角放大镜或 Ctrl+F 打开搜索；Esc 先退出搜索，再关闭宝典。材料与词条详情链接可查看完整说明和材料来源。

材料属性、词条、工具、强化及原料来源读取当前注册内容，支持扩展材料。多方块可拖动旋转，在模型上滚轮缩放，右击复位；上下箭头切换层数，循环箭头切换逐层搭建动画。物品图标和词条支持原生悬浮提示。

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

结构各轴最多 32 格，坐标从零开始；后写入的方块覆盖同一位置。预览不会在世界中放置方块。
